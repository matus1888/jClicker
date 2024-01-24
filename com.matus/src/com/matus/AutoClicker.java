package com.matus;

import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.github.kwhat.jnativehook.GlobalScreen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class AutoClicker {
    private Robot robot;
    private JFrame frame;
    private JPanel panel;

    private JButton pickLocationButton;
    private JButton addPointButton;
    private JButton hotkeyButton;
    private JButton saveConfigButton;
    private JButton loadConfigButton;
    private JTextField intervalField;
    private JTextField xCoordinate;
    private JTextField yCoordinate;
    private JComboBox<String> mouseButtonComboBox;

    private Point location;
    private int interval;
    private int mouseButton;
    private boolean running;
    private boolean hotkeySet;
    private int hotkeyCode;
    private ArrayList<Point> points;


    public void start() {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
        System.out.println("loation()");

        frame = new JFrame("AutoClicker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (hotkeySet && e.getKeyCode() == hotkeyCode) {
                    toggleAutoClick();
                }
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });

        panel = new JPanel();

        pickLocationButton = new JButton("Pick Location");
        pickLocationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pickLocation();
            }
        });

        addPointButton = new JButton("Add Point");
        addPointButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addPoint();
            }
        });

        hotkeyButton = new JButton("Set Hotkey (F2)");
        hotkeyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setHotkey();
            }
        });

        saveConfigButton = new JButton("Save Config");
        saveConfigButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveConfig();
            }
        });

        loadConfigButton = new JButton("Load Config");
        loadConfigButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadConfig();
            }
        });

        intervalField = new JTextField(10);
        intervalField.setText("1000");

        xCoordinate = new JTextField(10);
        yCoordinate = new JTextField(10);

        mouseButtonComboBox = new JComboBox<String>(new String[]{"Left", "Middle", "Right"});

        panel.add(new JLabel("Interval (ms):"));
        panel.add(intervalField);
        panel.add(new JLabel("Mouse Button:"));
        panel.add(mouseButtonComboBox);
        panel.add(pickLocationButton);
        panel.add(addPointButton);
        panel.add(hotkeyButton);
        panel.add(saveConfigButton);
        panel.add(loadConfigButton);
        panel.add(xCoordinate);
        panel.add(yCoordinate);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

        location = MouseInfo.getPointerInfo().getLocation();
        running = false;
        hotkeySet = false;
        hotkeyCode = KeyEvent.VK_F2;
        points = new ArrayList<Point>();
    }

    private void pickLocation() {
        frame.setVisible(false);



        GlobalScreen.addNativeMouseListener(new NativeMouseListener() {
            public void nativeMouseClicked(NativeMouseEvent e) {
                if (e.getButton() == NativeMouseEvent.BUTTON1) {
                    // Действия при нажатии левой кнопки мыши на уровне операционной системы
                    System.out.println("Левая кнопка мыши нажата");
                    location = e.getPoint();
                    xCoordinate.setText(String.valueOf(location.x));
                    yCoordinate.setText(String.valueOf(location.y));
                    GlobalScreen.removeNativeMouseListener(this);
                    frame.setVisible(true);
                }
            }

            public void nativeMousePressed(NativeMouseEvent e) {
            }

            public void nativeMouseReleased(NativeMouseEvent e) {
            }
        });
    }

    private void addPoint() {
        points.add(location);
        System.out.println(points);
    }

    private void toggleAutoClick() {
        if (running) {
            running = false;
            pickLocationButton.setText("Pick Location");
            addPointButton.setEnabled(true);
        } else {
            running = true;
            pickLocationButton.setText("Add Point");
            addPointButton.setEnabled(false);
        }
    }

    private void setHotkey() {
        hotkeyButton.setText("Press any key...");
        hotkeyButton.setEnabled(false);
        hotkeySet = false;

        frame.requestFocus();
        frame.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                hotkeyCode = e.getKeyCode();
                hotkeyButton.setText("Set Hotkey (" + KeyEvent.getKeyText(hotkeyCode) + ")");
                hotkeyButton.setEnabled(true);
                hotkeySet = true;
                frame.removeKeyListener(this);
                frame.requestFocus();
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });
    }

    private void saveConfig() {
        JSONObject configJSON = getJsonObject();

        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(configJSON.toJSONString());
                writer.flush();
                JOptionPane.showMessageDialog(frame, "Config saved successfully.", "Save Config", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Failed to save config.", "Save Config", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JSONObject getJsonObject() {
        JSONObject configJSON = new JSONObject();
        JSONArray pointsJSON = new JSONArray();

        configJSON.put("interval", intervalField.getText());
        configJSON.put("mouseButton", mouseButtonComboBox.getSelectedIndex());

        points.forEach((point) -> {
            JSONObject pointJSON = new JSONObject();
            pointJSON.put("x", point.getX());
            pointJSON.put("y", point.getY());
            pointsJSON.add(pointJSON);
        });

        configJSON.put("points", pointsJSON);
        return configJSON;
    }

    private void loadConfig() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                FileReader reader = new FileReader(file);
                JSONParser parser = new JSONParser();
                JSONObject configJSON = (JSONObject) parser.parse(reader);

                intervalField.setText((String) configJSON.get("interval"));
                mouseButtonComboBox.setSelectedIndex(((Number) configJSON.get("mouseButton")).intValue());

                points.clear();
                JSONArray pointsJSON = (JSONArray) configJSON.get("points");
                for (Object obj : pointsJSON) {
                    JSONObject pointJSON = (JSONObject) obj;
                    double x = ((Number) pointJSON.get("x")).doubleValue();
                    double y = ((Number) pointJSON.get("y")).doubleValue();
                    points.add(new Point((int) x, (int) y));
                }

                JOptionPane.showMessageDialog(frame, "Config loaded successfully.", "Load Config", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Failed to load config.", "Load Config", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void autoClick(int x, int y) {
        interval = Integer.parseInt(intervalField.getText());
        mouseButton = mouseButtonComboBox.getSelectedIndex();

        while (true) {
            if (running) {
                robot.mouseMove(x, y);

                if (mouseButton == 0) {
                    robot.mousePress(InputEvent.BUTTON1_MASK);
                    robot.mouseRelease(InputEvent.BUTTON1_MASK);
                } else if (mouseButton == 1) {
                    robot.mousePress(InputEvent.BUTTON2_MASK);
                    robot.mouseRelease(InputEvent.BUTTON2_MASK);
                } else if (mouseButton == 2) {
                    robot.mousePress(InputEvent.BUTTON3_MASK);
                    robot.mouseRelease(InputEvent.BUTTON3_MASK);
                }

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
