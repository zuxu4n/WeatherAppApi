package weatherApp.src;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class WeatherAppGui extends JFrame {
    private JSONObject weatherData;
    private final String API_KEY = "asdf"; // your OpenWeatherMap API key

    private JWindow suggestionWindow;
    private JList<String> suggestionList;
    private JLabel errorLabel;
    private AnimatedCloudComponent weatherAnimationComponent;

    public WeatherAppGui() {
        super("Weather App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 650);
        setLocationRelativeTo(null);
        setLayout(null);
        setResizable(false);
        getContentPane().setBackground(new Color(134, 198, 247));
        addGuiComponents();
    }

    private void addGuiComponents() {
        JTextField searchTextField = new JTextField();
        searchTextField.setBounds(15, 15, 351, 45);
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));
        add(searchTextField);

        JButton searchButton = new JButton(loadImage("weatherApp/src/assets/search.png"));
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);
        add(searchButton);

        // --- FIXED: Suggestion dropdown with proper focus handling ---
        suggestionWindow = new JWindow(this);
        suggestionWindow.setFocusable(false); // Prevent window from stealing focus
        suggestionWindow.setFocusableWindowState(false); // Additional focus prevention

        suggestionList = new JList<>();
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setFocusable(false); // Prevent list from stealing focus

        JScrollPane scrollPane = new JScrollPane(suggestionList);
        scrollPane.setFocusable(false); // Prevent scroll pane from stealing focus
        suggestionWindow.add(scrollPane);
        suggestionWindow.setSize(351, 100); // Match width to text field

        suggestionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                String selected = suggestionList.getSelectedValue();
                if (selected != null) {
                    searchTextField.setText(selected);
                    suggestionWindow.setVisible(false);
                    // Use SwingUtilities.invokeLater to ensure text field keeps focus
                    SwingUtilities.invokeLater(() -> {
                        searchTextField.requestFocusInWindow();
                        searchButton.doClick();
                    });
                }
            }
        });

        // Add keyboard navigation for suggestions
        searchTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!suggestionWindow.isVisible())
                    return;

                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_DOWN) {
                    e.consume();
                    int selectedIndex = suggestionList.getSelectedIndex();
                    if (selectedIndex < suggestionList.getModel().getSize() - 1) {
                        suggestionList.setSelectedIndex(selectedIndex + 1);
                    }
                } else if (keyCode == KeyEvent.VK_UP) {
                    e.consume();
                    int selectedIndex = suggestionList.getSelectedIndex();
                    if (selectedIndex > 0) {
                        suggestionList.setSelectedIndex(selectedIndex - 1);
                    }
                } else if (keyCode == KeyEvent.VK_ENTER) {
                    e.consume();
                    String selected = suggestionList.getSelectedValue();
                    if (selected != null) {
                        searchTextField.setText(selected);
                        suggestionWindow.setVisible(false);
                        searchButton.doClick();
                    }
                } else if (keyCode == KeyEvent.VK_ESCAPE) {
                    e.consume();
                    suggestionWindow.setVisible(false);
                }
            }
        });

        searchTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> showSuggestions());
            }

            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> showSuggestions());
            }

            public void changedUpdate(DocumentEvent e) {
            }

            private void showSuggestions() {
                String input = searchTextField.getText().trim();
                if (input.length() < 2) {
                    suggestionWindow.setVisible(false);
                    return;
                }

                // Hide error when user starts typing
                errorLabel.setVisible(false);

                List<String> suggestions = getLocationSuggestions(input);
                if (suggestions.isEmpty()) {
                    suggestionWindow.setVisible(false);
                    return;
                }

                suggestionList.setListData(suggestions.toArray(new String[0]));
                suggestionList.setSelectedIndex(0); // Select first item by default

                Point location = searchTextField.getLocationOnScreen();
                suggestionWindow.setLocation(location.x, location.y + searchTextField.getHeight());
                suggestionWindow.setVisible(true);

                // Ensure text field retains focus
                searchTextField.requestFocusInWindow();
            }
        });

        // Hide suggestions when text field loses focus (with delay to allow clicking)
        searchTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // Use timer to delay hiding so mouse clicks on suggestions work
                Timer timer = new Timer(150, evt -> suggestionWindow.setVisible(false));
                timer.setRepeats(false);
                timer.start();
            }
        });

        // Replace static weather image with animated component
        weatherAnimationComponent = new AnimatedCloudComponent();
        weatherAnimationComponent.setBounds(0, 125, 450, 217);
        add(weatherAnimationComponent);

        JLabel temperatureText = new JLabel("Welcome");
        temperatureText.setBounds(0, 350, 450, 54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        JLabel weatherConditionDesc = new JLabel("Enter a Location!");
        weatherConditionDesc.setBounds(0, 405, 450, 36);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        JLabel humidityImage = new JLabel(loadImage("weatherApp\\src\\assets\\humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        // add(humidityImage);

        JLabel humidityText = new JLabel("<html><b>Humidity</b> 100%</html>");
        humidityText.setBounds(90, 500, 85, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        // add(humidityText);

        JLabel windspeedImage = new JLabel(loadImage("weatherApp\\src\\assets\\windspeed.png"));
        windspeedImage.setBounds(220, 500, 74, 66);
        // add(windspeedImage);

        JLabel windspeedText = new JLabel("<html><b>Windspeed</b> 15km/h</html>");
        windspeedText.setBounds(310, 500, 85, 55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        // add(windspeedText);

        // Error message label (initially hidden)
        errorLabel = new JLabel();
        errorLabel.setBounds(15, 70, 410, 30);
        errorLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        errorLabel.setForeground(Color.RED);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        errorLabel.setVisible(false);
        add(errorLabel);

        searchButton.addActionListener(e -> {
            String userInput = searchTextField.getText();
            if (userInput.replaceAll("\\s", "").isEmpty()) {
                errorLabel.setText("Please enter a location");
                errorLabel.setVisible(true);
                return;
            }

            // Hide suggestions and any previous error when searching
            suggestionWindow.setVisible(false);
            errorLabel.setVisible(false);

            weatherData = appFinal.getWeatherData(userInput);
            if (weatherData == null) {
                errorLabel.setText("Location not found. Please try a different location.");
                errorLabel.setVisible(true);
                return;
            }

            String weatherCondition = (String) weatherData.get("weather_condition");

            // Update animated component instead of static image

            add(humidityImage);
            add(humidityText);
            add(windspeedImage);
            add(windspeedText);

            switch (weatherCondition) {
                case "Clear":
                    getContentPane().setBackground(new Color(134, 198, 247));
                    break;
                case "Cloudy":
                    getContentPane().setBackground(new Color(98, 134, 166));
                    break;
                case "Rain":
                    getContentPane().setBackground(new Color(87, 96, 105));
                    break;
                case "Snow":
                    getContentPane().setBackground(new Color(167, 193, 219));
                    break;
            }

            weatherAnimationComponent.setWeatherCondition(weatherCondition);

            double temperature = (double) weatherData.get("temperature");
            temperatureText.setText(temperature + " C");

            weatherConditionDesc.setText(weatherCondition);

            long humidity = (long) weatherData.get("humidity");
            humidityText.setText("<html><b>Humidity</b> " + humidity + "%</html>");

            double windspeed = (double) weatherData.get("wind_speed");
            windspeedText.setText("<html><b>Windspeed</b> " + windspeed + "km/h</html>");
        });
    }

    private ImageIcon loadImage(String resourcePath) {
        try {
            BufferedImage image = ImageIO.read(new File(resourcePath));
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Static list of locations
    private static final String[] LOCATIONS = {
            "New York", "Los Angeles", "Chicago", "Houston", "Phoenix",
"Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose",
"London", "Manchester", "Birmingham", "Liverpool", "Glasgow",
"Edinburgh", "Aberdeen", "Inverness", // Scotland additions
"Toronto", "Vancouver", "Montreal", "Calgary", "Ottawa",
"Quebec", "Winnipeg", "Halifax", "Victoria", // More Canada
"Tokyo", "Osaka", "Kyoto", "Yokohama", "Nagoya",
"Paris", "Lyon", "Marseille", "Toulouse", "Nice",
"Lille", "Bordeaux", "Strasbourg", "Nantes", // More France
"Berlin", "Munich", "Hamburg", "Cologne", "Frankfurt",
"Stuttgart", "Düsseldorf", "Leipzig", "Dresden", // More Germany
"Rome", "Milan", "Naples", "Turin", "Palermo",
"Florence", "Venice", "Bologna", "Genoa", // Italy
"Sydney", "Melbourne", "Brisbane", "Perth", "Adelaide",
"Canberra", "Hobart", "Darwin", // More Australia
"Mumbai", "Delhi", "Bangalore", "Chennai", "Kolkata",
"Hyderabad", "Ahmedabad", "Pune", "Jaipur", // More India
"Beijing", "Shanghai", "Guangzhou", "Shenzhen", "Chengdu",
"Hangzhou", "Wuhan", "Nanjing", "Xi'an", // More China
"São Paulo", "Rio de Janeiro", "Brasília", "Salvador", "Fortaleza",
"Belo Horizonte", "Curitiba", "Manaus", // More Brazil
"Mexico City", "Guadalajara", "Monterrey", "Puebla", "Tijuana",
"Moscow", "Saint Petersburg", "Novosibirsk", "Yekaterinburg", "Kazan",
"Vladivostok", "Sochi", "Samara", // More Russia
"Seoul", "Busan", "Incheon", "Daegu", // South Korea
"Bangkok", "Chiang Mai", "Phuket", // Thailand
"Hong Kong", "Taipei", "Singapore", "Jakarta", "Hanoi",
"Ho Chi Minh City", "Manila", "Kuala Lumpur", "Istanbul", "Dubai",
"Abu Dhabi", "Doha", "Riyadh", "Jeddah", "Cape Town",
"Johannesburg", "Nairobi", "Casablanca", "Lagos", "Accra",
"Cairo", "Alexandria", "Tunis", "Algiers", "Tehran",
"Baghdad", "Jerusalem", "Amman", "Beirut", "Damascus",
"Antarctica", "McMurdo Station"

    };

    private List<String> getLocationSuggestions(String query) {
        List<String> suggestions = new ArrayList<>();
        String queryLower = query.toLowerCase();

        for (String location : LOCATIONS) {
            if (location.toLowerCase().contains(queryLower)) {
                suggestions.add(location);
                if (suggestions.size() >= 5) { // Limit to 5 suggestions
                    break;
                }
            }
        }

        return suggestions;
    }
}
