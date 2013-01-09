package fractalFlameV3;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.imageio.stream.FileImageInputStream;

import com.google.gson.GsonBuilder;

import fractalFlameV3.fractalGenome.FractalGenome;
import fractalFlameV3.fractalThread.FractalThread;
import fractalFlameV3.fractalThread.ThreadSignal;
import processing.core.PApplet;
import processing.core.PConstants;

public class Main extends PApplet {
	static boolean	fullscreen	    = false;

	int	            swid	        = 512;
	int	            shei	        = 512;
	int	            ss	            = 1;

	int	            fr	            = 60;

	Histogram	    h;
	FractalGenome	genome;
	FractalThread[]	threads;
	ThreadSignal	threadSignal;

	final int	    SYSTEM_THREADS	= Runtime.getRuntime().availableProcessors();

	// by running SYSTEM_THREADS - 2 threads we can hopefully avoid making the system unusalbe while
	// the flame is generating. There is of course a tradeoff in generation speed, which doesn't
	// really matter to me as I have a 12 thread system.
	final int	    maxFlameThreads	= (SYSTEM_THREADS > 3) ? SYSTEM_THREADS - 2 : 1;

	public static final void main(final String args[]) {
		if (Main.fullscreen) {
			PApplet.main(new String[] { "--present", "fractalFlameV3.Main" });
		} else {
			PApplet.main(new String[] { "fractalFlameV3.Main" });
		}
	}

	@Override
	public void setup() {
		swid = (fullscreen) ? displayWidth : swid;
		shei = (fullscreen) ? displayHeight : shei;
		this.size(swid, shei);
		frameRate(fr);

		h = newHistogram();

		genome = loadLastGenome();
		threads = new FractalThread[1];
		threadSignal = new ThreadSignal();
		startThreads();

	}

	private FractalGenome loadLastGenome() {
		String fullGenomeString = "";
		GsonBuilder gb = new GsonBuilder();
		try {
			FileReader fileReader = new FileReader("images/last.fractalgenome");
			BufferedReader lastGenomeReader = new BufferedReader(fileReader);

			String line;
			while ((line = lastGenomeReader.readLine()) != null) {
				fullGenomeString += line;
			}

			lastGenomeReader.close();
			fileReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(fullGenomeString);
		return gb.create().fromJson(fullGenomeString, new FractalGenome(3, 3).getClass());
	}

	private FractalGenome newGenome() {
		final FractalGenome fg = new FractalGenome(3, 10);
		fg.variationToggle = true;
		fg.finalTransformToggle = true;
		fg.setLogScale();
		genome.center = true;
		return fg;
	}

	private Histogram newHistogram() {
		final Histogram h = new Histogram(swid, shei, ss);
		return h;
	}

	@Override
	public void keyPressed() {
		stopThreads();

		if ('h' == Character.toLowerCase(key)) {
			ss = (ss == 1) ? 15 : 1;
			h = null;
			System.gc();
			h = newHistogram();
			System.out.println("# SS\t|\t " + ss);
		}

		if ('r' == Character.toLowerCase(key)) {
			genome = newGenome();
			h.reset();
		}

		if ('t' == Character.toLowerCase(key)) {
			threads = new FractalThread[(threads.length == 8) ? 1 : 8];
			System.out.println("# TH\t|\t " + threads.length);
		}

		if ('f' == Character.toLowerCase(key)) {
			genome.finalTransformToggle = !genome.finalTransformToggle;
			System.out.println("# FT\t|\t " + genome.finalTransformToggle);
			h.reset();
		}

		if ('v' == Character.toLowerCase(key)) {
			genome.variationToggle = !genome.variationToggle;
			System.out.println("# VT\t|\t " + genome.variationToggle);
			h.reset();
		}

		if ('s' == Character.toLowerCase(key)) {
			String fileName = "images/" + genome.hashCode() + "_#####.bmp";
			saveFrame(fileName);
			genome.saveGsonRepresentation();

		}

		if (('+' == key) || ('=' == key)) {
			genome.cameraXShrink /= 1.01;
			genome.cameraYShrink /= 1.01;
			h.reset();
		}

		if (('-' == key) || ('_' == key)) {
			genome.cameraXShrink *= 1.01;
			genome.cameraYShrink *= 1.01;
			h.reset();
		}
		if (keyCode == PConstants.UP) {
			genome.cameraYOffset += .01 * genome.cameraYShrink;
			h.reset();
		}
		if (keyCode == PConstants.DOWN) {
			genome.cameraYOffset -= .01 * genome.cameraYShrink;
			h.reset();
		}

		if (keyCode == PConstants.LEFT) {
			genome.cameraXOffset += .01 * genome.cameraXShrink;
			h.reset();
		}

		if (keyCode == PConstants.RIGHT) {
			genome.cameraXOffset -= .01 * genome.cameraXShrink;
			h.reset();
		}
		startThreads();
	}

	private void stopThreads() {
		threadSignal.running = false;
		for (final Thread t : threads) {
			try {
				t.join();
			} catch (final InterruptedException e) {
				System.out.println(e.getLocalizedMessage());
			}
		}
	}

	private void startThreads() {
		threadSignal.running = true;
		for (final int i : Utils.range(threads.length)) {
			threads[i] = new FractalThread(genome, threadSignal, h);
		}
		for (final Thread t : threads) {
			t.start();
		}
	}

	@Override
	public void draw() {
		if (frameCount == 1) {
			loadPixels();
		} else {
			h.updatePixels(pixels, genome);
			this.updatePixels();
		}
	}
}
