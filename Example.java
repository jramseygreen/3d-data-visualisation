import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

//956213 Joshua Green - I declare this is my own work.

public class Example extends Application {
	short cthead[][][]; // store the 3D volume data set
	short min, max; // min/max value in the 3D volume data set
	int baseObjectNumber; // number of objects displayed before thumbnails

	@Override
	public void start(Stage stage) throws FileNotFoundException, IOException {
		stage.setTitle("CThead Viewer");

		ReadData();

		int width = 256;
		int height = 256;
		int heightAdjustment = 143; // remove from the height for y and x views
		// 1. We create an image we can write to
		WritableImage medical_image1 = new WritableImage(width, height);
		WritableImage medical_image2 = new WritableImage(width, height - heightAdjustment);
		WritableImage medical_image3 = new WritableImage(width, height - heightAdjustment);
		// 2. We create a view of that image
		ImageView imageView1 = new ImageView(medical_image1);
		ImageView imageView2 = new ImageView(medical_image2);
		ImageView imageView3 = new ImageView(medical_image3);

		Button thumbButton1 = new Button("thumbnails 1");
		Button thumbButton2 = new Button("thumbnails 2");
		Button thumbButton3 = new Button("thumbnails 3");
		Button MipButton = new Button("MIP");
		Button close = new Button("Exit");
		Button close2 = new Button("Exit");

		// sliders to step through the slices
		Slider zslider = new Slider(0, 112, 0);
		Slider yslider = new Slider(0, 255, 0);
		Slider xslider = new Slider(0, 255, 0);
		Slider zSizeSlider = new Slider(0, 256, 256 / 2);
		Slider ySizeSlider = new Slider(0, 256, 256 / 2);
		Slider xSizeSlider = new Slider(0, 256, 256 / 2);
		Slider thumbSizeSlider = new Slider(0, 100, 25);

		FlowPane root = new FlowPane();
		Scene scene = new Scene(root, 640, 480);

		imageView1.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				// determine zoom by current zoom slider value
				int multiplier = (int) (zSizeSlider.getValue() / (zSizeSlider.getMax() / 2));
				int[] slice = { (int) zslider.getValue(), -1, -1 };
				if (multiplier < 1)
					multiplier++;

				WritableImage image = new WritableImage((int) (width * multiplier), (int) (height * multiplier));
				ImageView imageView = new ImageView(image);
				Button backButton = new Button("back");

				float[][] pixelList = resize(image, (int) (zSizeSlider.getMax() * multiplier), slice);

				FlowPane enhanced = new FlowPane();
				enhanced.getChildren().addAll(close2, imageView, backButton);
				stage.setScene(new Scene(enhanced));
				stage.setFullScreen(true);
				writePixels(pixelList, image);

				backButton.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						stage.setScene(scene);
						stage.setFullScreen(true);
					}
				});
			}
		});

		imageView2.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				int multiplier = (int) (ySizeSlider.getValue() / (ySizeSlider.getMax() / 2));
				int[] slice = { -1, (int) yslider.getValue(), -1 };
				if (multiplier < 1)
					multiplier++;

				WritableImage image = new WritableImage((int) (width * multiplier),
						(int) ((height - heightAdjustment) * multiplier));
				ImageView imageView = new ImageView(image);
				Button backButton = new Button("back");

				float[][] pixelList = resize(image, (int) ((int) ySizeSlider.getMax() * multiplier), slice);

				FlowPane enhanced = new FlowPane();
				enhanced.getChildren().addAll(close2, imageView, backButton);
				stage.setScene(new Scene(enhanced));
				stage.setFullScreen(true);
				writePixels(pixelList, image);

				backButton.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						stage.setScene(scene);
						stage.setFullScreen(true);
					}
				});
			}
		});

		imageView3.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				int multiplier = (int) xSizeSlider.getValue() / (int) (xSizeSlider.getMax() / 2);
				int[] slice = { -1, -1, (int) xslider.getValue() };
				if (multiplier < 1)
					multiplier++;

				WritableImage image = new WritableImage((int) (width * multiplier),
						(int) ((height - heightAdjustment) * multiplier));
				ImageView imageView = new ImageView(image);
				Button backButton = new Button("back");

				float[][] pixelList = resize(image, (int) ((int) xSizeSlider.getMax() * multiplier), slice);

				FlowPane enhanced = new FlowPane();
				enhanced.getChildren().addAll(close2, imageView, backButton);
				stage.setScene(new Scene(enhanced));
				stage.setFullScreen(true);

				writePixels(pixelList, image);

				backButton.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						stage.setScene(scene);
						stage.setFullScreen(true);
					}
				});
			}
		});

		thumbButton1.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				int[] slice = { 1, -1, -1 };
				makeThumbnails(root, medical_image1, slice, zslider, thumbSizeSlider);
			}
		});

		thumbButton2.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				int[] slice = { -1, 1, -1 };
				makeThumbnails(root, medical_image2, slice, yslider, thumbSizeSlider);
			}
		});

		thumbButton3.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				int[] slice = { -1, -1, 1 };
				makeThumbnails(root, medical_image3, slice, xslider, thumbSizeSlider);
			}
		});

		MipButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				MIP(medical_image1, medical_image2, medical_image3, zSizeSlider, ySizeSlider, xSizeSlider);
			}
		});

		zslider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				pixelClear(medical_image1);
				int[] slice = { newValue.intValue(), -1, -1 };
				float[][] pixelList = resize(medical_image1, (int) zSizeSlider.getValue(), slice);
				writePixels(pixelList, medical_image1);
			}

		});

		yslider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				pixelClear(medical_image2);
				int[] slice = { -1, newValue.intValue(), -1 };
				float[][] pixelList = resize(medical_image2, (int) ySizeSlider.getValue(), slice);
				writePixels(pixelList, medical_image2);
			}

		});
		xslider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				pixelClear(medical_image3);
				int[] slice = { -1, -1, newValue.intValue() };
				float[][] pixelList = resize(medical_image3, (int) xSizeSlider.getValue(), slice);
				writePixels(pixelList, medical_image3);
			}

		});

		zSizeSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				pixelClear(medical_image1);
				int[] slice = { (int) zslider.getValue(), -1, -1 };
				float[][] pixelList = resize(medical_image1, newValue.intValue(), slice);
				writePixels(pixelList, medical_image1);
			}

		});

		ySizeSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				pixelClear(medical_image2);
				int[] slice = { -1, (int) yslider.getValue(), -1 };
				float[][] pixelList = resize(medical_image2, newValue.intValue(), slice);
				writePixels(pixelList, medical_image2);
			}

		});

		xSizeSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				pixelClear(medical_image3);
				int[] slice = { -1, -1, (int) xslider.getValue() };
				float[][] pixelList = resize(medical_image3, newValue.intValue(), slice);
				writePixels(pixelList, medical_image3);
			}

		});
		
		close.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.exit(0);
			}
		});
		close2.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.exit(0);
			}
		});

		root.setVgap(8);
		root.setHgap(4);

		root.getChildren().addAll(close, imageView1, imageView2, imageView3, MipButton, zslider, yslider, xslider, zSizeSlider,
				ySizeSlider, xSizeSlider, thumbButton1, thumbButton2, thumbButton3, thumbSizeSlider);

		baseObjectNumber = root.getChildren().size();

		stage.setScene(scene);
		stage.setFullScreen(true);
		stage.show();
	}

	// allows for rectangular images
	public int calcHeight(WritableImage image, int size) {
		float ratio = (float) (size / image.getWidth());
		return (int) (ratio * image.getHeight());
	}

	// billinear resize function
	public float[][] resize(WritableImage image, int size, int[] slice) {
		int top = slice[0];
		int side = slice[1];
		int front = slice[2];
		int w = (int) image.getWidth(), h = calcHeight(image, (int) size);
		float caa = 0, cbb = 0, cab = 0, cba = 0;
		float[][] pixelList = new float[(int) size][h];
		// Shows how to loop through each pixel and colour
		// Try to always use j for loops in y, and i for loops in x
		// as this makes the code more readable

		for (int j = 0; j < h; j++) {
			for (int i = 0; i < size; i++) {
				float y, x;

				// allows upscaling - pixel target adjusted to match original scale
				int ratio;
				if (w > 256) {
					ratio = ((w / 2) / 256) * 2;
				} else {
					ratio = 1;
				}
				
				y = (float) ((j / ratio) * (float) ((float) (image.getHeight() / ratio) / (h / ratio)));
				x = (float) ((i / ratio) * (float) ((float) (image.getWidth() / ratio) / (float) (size / ratio)));

				int ax = (int) Math.floor(x);
				int ay = (int) Math.ceil(y);

				int bx = (int) Math.floor(x);
				int by = (int) Math.ceil(y);

				// get the four points of colour
				if (top != -1) {
					caa = getColVal(cthead[top][ay][ax]);
					cbb = getColVal(cthead[top][by][bx]);
					cab = getColVal(cthead[top][ay][bx]);
					cba = getColVal(cthead[top][by][ax]);
				}
				if (side != -1) {
					caa = getColVal(cthead[ay][ax][side]);
					cbb = getColVal(cthead[by][bx][side]);
					cab = getColVal(cthead[ay][bx][side]);
					cba = getColVal(cthead[by][ax][side]);
				}
				if (front != -1) {
					caa = getColVal(cthead[ay][front][ax]);
					cbb = getColVal(cthead[by][front][bx]);
					cab = getColVal(cthead[ay][front][bx]);
					cba = getColVal(cthead[by][front][ax]);
				}

				// bilinear interpolation to find the single colour value required
				pixelList[i][j] = linearInterp(linearInterp(caa, cbb, x), linearInterp(cab, cba, x), y);
			} // column loop
		} // row loop
		return pixelList;
	}

	// writes a 2d pixelList/pixelmap to writable image
	public void writePixels(float[][] pixelList, WritableImage image) {

		PixelWriter image_writer = image.getPixelWriter();

		for (int i = 0; i < pixelList.length; i++) {
			for (int j = 0; j < pixelList[i].length; j++) {
				float col = pixelList[i][j];
				image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
			}
		}
	}

	public float linearInterp(float c1, float c2, float position) {
		// convert to decimal scale
		float ratio = (float) (position - Math.floor(position));
		return c1 + (c2 - c1) * ratio;
	}

	public float getColVal(short datum) {
		return (float) (datum - min) / (float) (max - min);
	}

	public void pixelClear(WritableImage image) {
		PixelWriter writer = image.getPixelWriter();

		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				writer.setColor(i, j, new Color(255 / 255.0, 255 / 255.0, 255 / 255.0, 0.0));
			}
		}
	}

	public void makeThumbnails(FlowPane root, WritableImage baseImage, int[] slice, Slider slider1, Slider slider2) {
		// clears any previous thumbnail view
		while (root.getChildren().size() != baseObjectNumber) {
			root.getChildren().remove(root.getChildren().size() - 1);
		}

		int thumbnailSize = (int) slider2.getValue();
		int top = slice[0];
		int side = slice[1];
		int front = slice[2];
		ArrayList<float[][]> data = new ArrayList<>();

		// add all the resized slices to the data array
		if (top != -1) {
			for (int i = 0; i < 113; i++) {
				slice[0] = i;
				data.add(resize(baseImage, thumbnailSize, slice));
			}
		}
		if (side != -1) {
			for (int i = 0; i < 255; i++) {
				slice[1] = i;
				data.add(resize(baseImage, thumbnailSize, slice));
			}
		}
		if (front != -1) {
			for (int i = 0; i < 255; i++) {
				slice[2] = i;
				data.add(resize(baseImage, thumbnailSize, slice));
			}
		}

		// size of image
		int x = data.get(0).length;
		int y = data.get(0)[0].length;

		// create an image and image view for each 2d image in data array
		for (int e = 0; e < data.size(); e++) {
			float[][] pixelList = data.get(e);
			WritableImage image = new WritableImage(x, y);
			ImageView view = new ImageView(image);
			root.getChildren().add(view);
			writePixels(pixelList, image); // write the pixels to the image

			// slice for event handler
			int[] sliceClone = { -1, -1, -1 };

			for (int i = 0; i < slice.length; i++) {
				if (slice[i] != -1) {
					sliceClone[i] = e;
				}
			}

			view.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					writePixels(resize(baseImage, 256, sliceClone), baseImage);
					for (int i : sliceClone) {
						if (i != -1) {
							slider1.setValue(i);
						}
					}
				}
			});
		}

	}

	public void MIP(WritableImage image1, WritableImage image2, WritableImage image3, Slider zslider, Slider yslider,
			Slider xslider) {
		zslider.setValue(zslider.getMax());
		yslider.setValue(yslider.getMax());
		xslider.setValue(xslider.getMax());

		PixelWriter image_writer;
		WritableImage[] images = { image1, image2, image3 };

		for (WritableImage image : images) {
			image_writer = image.getPixelWriter();
			for (int j = 0; j < image.getHeight(); j++) {
				for (int i = 0; i < image.getWidth(); i++) {
					int maximum = 1;

					// sets the maximum depth to slice to sepending on if it is
					// top side or front
					int zmax = 0;
					if (image == image1) {
						zmax = 113;
					} else {
						zmax = 256;
					}
					for (int z = 0; z < zmax; z++) {
						if (image == image1) {
							maximum = Math.max(maximum, cthead[z][j][i]);
						}
						if (image == image2) {
							maximum = Math.max(maximum, cthead[j][i][z]);
						}
						if (image == image3) {
							maximum = Math.max(maximum, cthead[j][z][i]);
						}
					}
					float col = (((float) maximum - (float) min) / ((float) (max - min)));

					image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
				}
			}

		}

	}

	// Function to read in the cthead data set
	public void ReadData() throws IOException {
		// File name is hardcoded here - much nicer to have a dialog to select
		// it and capture the size from the user
		File file = new File("CThead.raw");
		// Read the data quickly via a buffer (in C++ you can just do a single
		// fread - I couldn't find if there is an equivalent in Java)
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

		int i, j, k; // loop through the 3D data set

		min = Short.MAX_VALUE;
		max = Short.MIN_VALUE; // set to extreme values
		short read; // value read in
		int b1, b2; // data is wrong Endian (check wikipedia) for Java so we
					// need to swap the bytes around

		cthead = new short[113][256][256]; // allocate the memory - note this is
											// fixed for this data set
		// loop through the data reading it in
		for (k = 0; k < 113; k++) {
			for (j = 0; j < 256; j++) {
				for (i = 0; i < 256; i++) {
					// because the Endianess is wrong, it needs to be read byte
					// at a time and swapped
					b1 = ((int) in.readByte()) & 0xff; // the 0xff is because
														// Java does not have
														// unsigned types
					b2 = ((int) in.readByte()) & 0xff; // the 0xff is because
														// Java does not have
														// unsigned types
					read = (short) ((b2 << 8) | b1); // and swizzle the bytes
														// around
					if (read < min)
						min = read; // update the minimum
					if (read > max)
						max = read; // update the maximum
					cthead[k][j][i] = read; // put the short into memory (in C++
											// you can replace all this code
											// with one fread)
				}
			}
		}
	}

	public static void main(String[] args) {
		launch();
	}
}