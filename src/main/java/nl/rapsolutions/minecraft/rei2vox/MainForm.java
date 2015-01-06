package nl.rapsolutions.minecraft.rei2vox;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by ronnie on 21-04-14.
 */
public class MainForm extends JFrame {
    private JTextArea eleTextArea;
    private JButton convertButton;
    private JButton browseButton;
    private JTextField editMCGameFolder;
    private JPanel rootPanel;

    public MainForm() {
        super("Rei2Voxel Converter");

        setContentPane(rootPanel);

        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Set Default Minecraft Dir based on the OS
        editMCGameFolder.setText(UserDir.getApplicationSupport());

        browseButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //Create a file chooser
                final JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return null;
                    }
                });

                int returnVal = fc.showOpenDialog(MainForm.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    editMCGameFolder.setText(file.getAbsolutePath());
                } else {
                    appendLog("Cancelled.\n");
                }
            }


        });
        convertButton.addActionListener(new ActionListener() {

            public HashMap<String, StringBuffer> voxelFilemap = new HashMap<String, StringBuffer>();

            @Override
            public void actionPerformed(ActionEvent e) {
                appendLog("Opening and Checking: " + editMCGameFolder.getText() + " for presence of 'mods/rei_minimap' folder.");
                File reiDataDir = new File(editMCGameFolder.getText(), "mods" + File.separatorChar + "rei_minimap");
                File voxelDataDir = new File(editMCGameFolder.getText(), "mods" + File.separatorChar + "VoxelMods" + File.separatorChar + "voxelMap");
                appendLog("Checking: " + reiDataDir.getAbsolutePath() + ".");
                if (!reiDataDir.exists()) {
                    appendLog("Could not locate: " + reiDataDir.getAbsolutePath());

                } else {
                    convert(reiDataDir, voxelDataDir);
                }
            }

            private void convert(File reiDataDir, File voxelDataDir) {
                appendLog("");
                appendLog("Reading Point Files...");
                File[] pointfiles = reiDataDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".points");
                    }
                });


                for (File pointfile : pointfiles) {
                    appendLog("Reading Point File: " + pointfile.getName());
                    ReiPointFile reiPointFile = new ReiPointFile(pointfile);
                    if (!voxelFilemap.containsKey(reiPointFile.getServer())) {
                        StringBuffer buffer = new StringBuffer();
                        SimpleDateFormat format = new SimpleDateFormat("YYYYMMddHHmm");
                        String format1 = format.format(new Date());
                        buffer.append("filetimestamp:" + format1 + "\r\n");
                        appendLog("Creating new Voxel file with timestamp: " + format1);
                        voxelFilemap.put(reiPointFile.getServer(), buffer);
                    }

                    StringBuffer voxelBuffer = voxelFilemap.get(reiPointFile.getServer());
                    int dimension = Integer.parseInt(reiPointFile.getDimension());
                    LineIterator it = null;
                    try {
                        it = FileUtils.lineIterator(pointfile, "UTF-8");
                        while (it.hasNext()) {
                            String line = it.nextLine();

                            String[] values = line.split("\\:");
                            String colorCode = values[5];
                            Color color = Color.decode("#" + colorCode);
							
							//Nether waypoints are stored at the surfacecoordinates the 8 multiply rule would lead to
							int x = Integer.parseInt(values[1]);
							
							int z = Integer.parseInt(values[3]);
							if(dimension==-1)
						    {
								x*=8;
								z*=8;
							}
                            // Incorporate the offset the signs are placed one higher in Rei's Map
                            String format = String.format(Locale.ENGLISH, "name:%s,x:%s,z:%s,y:%s,enabled:%s,red:%f,green:%f,blue:%f,suffix:,world:,dimensions:%s\r\n", values[0], x,z, "" + (Integer.parseInt(values[2]) - 1), values[4],
                                    (color.getRed() / 255f), (color.getGreen() / 255f), (color.getBlue() / 255f), dimension + "#");

//                            appendLog("Adding: " + format);
                            voxelBuffer.append(format);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (it != null)
                            it.close();
                    }
                }

                appendLog("");

                // Write all the Voxel files to the Voxel directory
                if (!voxelDataDir.exists()) {
                    voxelDataDir.mkdirs();
                }

                for(String key : voxelFilemap.keySet()) {
                    File voxelDataFile = new File(voxelDataDir, key + ".points");
                    if (voxelDataFile.exists()) {
                        appendLog("Voxel Data file already exists! not writing to this file: " + voxelDataFile.getAbsolutePath());
                        continue;
                    }

                    try {
                        FileUtils.writeStringToFile(voxelDataFile, voxelFilemap.get(key).toString());
                        appendLog("Wrote: " + voxelDataFile.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                        appendLog("Could not write: " + voxelDataFile.getAbsolutePath() + " Error: " + e.getMessage());
                    }
                }


            }
        });
    }

    private void appendLog(String text) {
        eleTextArea.append("\r\n" + text);
    }
}
