import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;

import java.net.URI;
import java.net.URISyntaxException;

//IMPORTS PARA SPOTIFY
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;


// GIT profe: al361883




//commit
public class APPMain {
    String versionDom = "1.2";
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JFrame mainFrame;

    public APPMain() {
        // Configurar la ventana principal
        JFrame frame = new JFrame("DOMOTIFY v" + versionDom + ": Transformando hogares, conectando vidas.");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(createLoginPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.getRootPane().setDefaultButton(loginButton);
        frame.setVisible(true);
    }



    private JPanel createLoginPanel() {
        JPanel loginPanel = new JPanel(new BorderLayout());

        // Cargar imagen de fondo
        ImageIcon backgroundImage = new ImageIcon("src/main/java/img/login.png");
        JLabel backgroundLabel = new JLabel(backgroundImage);
        loginPanel.add(backgroundLabel, BorderLayout.CENTER);
        backgroundLabel.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        JLabel usernameLabel = new JLabel("USUARIO DE DOMOTIFY");
        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(200, usernameField.getPreferredSize().height));
        usernameField.setColumns(12);
        usernameLabel.setForeground(Color.WHITE);

        JLabel passwordLabel = new JLabel("CONTRASEÑA O PIN TEMPORAL");
        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(200, passwordField.getPreferredSize().height));
        passwordField.setColumns(12);
        passwordLabel.setForeground(Color.WHITE);

        loginButton = new JButton("Iniciar sesión");
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                // Verificar el inicio de sesión
                String loginStatus = login(username, password);

                if (loginStatus.equals("true")) {
                    // Inicio de sesión exitoso
                    JOptionPane.showMessageDialog(null, "Inicio de sesión exitoso. Bienvenido.");

                    // Verificar el tipo de usuario
                    boolean isAdmin = isAdminUser(username);

                    // Abrir ventana de gestión de domótica
                    openDomoticsManagementWindow(isAdmin, username);
                } else if (loginStatus.equals("bad_credentials")) {
                    // Credenciales incorrectas
                    JOptionPane.showMessageDialog(null, "Credenciales incorrectas. Inténtalo de nuevo.");
                } else if (loginStatus.equals("user_not_found")) {
                    // Usuario no encontrado
                    showUserNotExistDialog(username, password);
                }
            }
        });


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.CENTER;

        formPanel.add(usernameLabel, gbc);

        gbc.gridy++;
        formPanel.add(usernameField, gbc);

        gbc.gridy++;
        formPanel.add(passwordLabel, gbc);

        gbc.gridy++;
        formPanel.add(passwordField, gbc);

        gbc.gridy++;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(loginButton, gbc);


        backgroundLabel.add(formPanel, BorderLayout.CENTER);
        loginPanel.add(Box.createVerticalGlue(), BorderLayout.NORTH);
        loginPanel.add(Box.createVerticalGlue(), BorderLayout.SOUTH);
        loginPanel.add(Box.createHorizontalGlue(), BorderLayout.WEST);
        loginPanel.add(Box.createHorizontalGlue(), BorderLayout.EAST);

        return loginPanel;
    }
//hola

    private int checkOwnerHouse(String username) {
        try {
            // Construir la URL de la petición HTTP GET
            String urlString = "https://domotify.net/api/check_ownerhouse.php";
            String query = String.format("username=%s", URLEncoder.encode(username, "UTF-8"));
            urlString += "?" + query;

            URL url = new URL(urlString);

            // Establecer la conexión HTTP
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Leer la respuesta del servidor
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();

            // Cerrar la conexión y el lector
            reader.close();
            connection.disconnect();

            // Analizar la respuesta del servidor
            return Integer.parseInt(response);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }
    private void showUserNotExistDialog(String username, String password) {
        int option = JOptionPane.showConfirmDialog(null,
                "El usuario no existe. ¿Deseas registrar una nueva cuenta con los datos proporcionados?\n\nUsuario: " + username + "\nContraseña: " + password,
                "Usuario no existe", JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            // Redirigir al usuario a la página de registro
            registerUser(username, password);
        }
    }
    private void registerUser(String username, String password) {
        try {
            // Construir la URL de la petición HTTP GET
            String urlString = "https://domotify.net/api/register.php";
            String query = String.format("username=%s&password=%s", URLEncoder.encode(username, "UTF-8"), URLEncoder.encode(password, "UTF-8"));
            urlString += "?" + query;

            URL url = new URL(urlString);

            // Establecer la conexión HTTP
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Leer la respuesta del servidor
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();

            // Cerrar la conexión y el lector
            reader.close();
            connection.disconnect();

            // Analizar la respuesta del servidor
            if (response.equals("success")) {
                JOptionPane.showMessageDialog(null, "Registro exitoso. Ahora puedes iniciar sesión.");
            } else {
                JOptionPane.showMessageDialog(null, "Error en el registro. Por favor, inténtalo de nuevo.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String login(String username, String password) {
        try {
            // Construir la URL de la petición HTTP GET
            String urlString = "https://domotify.net/api/login.php";
            String query = String.format("username=%s&password=%s", URLEncoder.encode(username, "UTF-8"), URLEncoder.encode(password, "UTF-8"));
            urlString += "?" + query;

            URL url = new URL(urlString);

            // Establecer la conexión HTTP
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Leer la respuesta del servidor
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();

            // Cerrar la conexión y el lector
            reader.close();
            connection.disconnect();

            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
    private boolean isAdminUser(String username) {
        try {
            // Construir la URL de la petición HTTP GET
            String urlString = "https://domotify.net/api/check_admin.php";
            String query = String.format("username=%s", URLEncoder.encode(username, "UTF-8"));
            urlString += "?" + query;

            URL url = new URL(urlString);

            // Establecer la conexión HTTP
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Leer la respuesta del servidor
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();

            // Cerrar la conexión y el lector
            reader.close();
            connection.disconnect();

            // Analizar la respuesta del servidor
            if (response.equals("1")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }



    private void openDomoticsManagementWindow(boolean isAdmin, String username) {
        int ownerHouse = checkOwnerHouse(username);

        if (ownerHouse != -1) {
            if (isAdmin) {
                openAdminDashboard(ownerHouse, username);
            } else {
                openChildDashboard();
            }
        } else {
            mensajeLlamada(ownerHouse, username);
        }
    }

    // CHECKEOS DE BASES DE DATOS POR PHP
    private boolean checkHasFridge(int houseId) {
        try {
            // Build the URL of the HTTP GET request
            String urlString = "https://domotify.net/api/fridge/getFridge.php";
            String query = String.format("houseId=%d", houseId);
            urlString += "?" + query;

            URL url = new URL(urlString);

            // Set up the HTTP connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read the server's response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();

            // Close the connection and reader
            reader.close();
            connection.disconnect();

            // Parse the server's response
            JSONObject jsonResponse = new JSONObject(response);
            String hasFridge = jsonResponse.getString("hasFridge");

            if (hasFridge.equals("1")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    private boolean checkHasSpoti(int houseId) {
        try {
            // Build the URL of the HTTP GET request
            String urlString = "https://domotify.net/api/spoti/getSpoti.php";
            String query = String.format("houseId=%d", houseId);
            urlString += "?" + query;

            URL url = new URL(urlString);

            // Set up the HTTP connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read the server's response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();

            // Close the connection and reader
            reader.close();
            connection.disconnect();

            // Parse the server's response
            JSONObject jsonResponse = new JSONObject(response);
            String hasSpoti = jsonResponse.getString("hasSpoti");

            if (hasSpoti.equals("1")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean checkHasMosquitos(int houseId) {
        try {
            // Build the URL of the HTTP GET request
            String urlString = "https://domotify.net/api/mosquitos/getMosquito.php";
            String query = String.format("houseId=%d", houseId);
            urlString += "?" + query;

            URL url = new URL(urlString);

            // Set up the HTTP connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read the server's response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();

            // Close the connection and reader
            reader.close();
            connection.disconnect();

            // Parse the server's response
            JSONObject jsonResponse = new JSONObject(response);
            String hasFridge = jsonResponse.getString("hasMosquito");

            if (hasFridge.equals("1")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean checkHasTemp(int houseId) {
        try {
            // Build the URL of the HTTP GET request
            String urlString = "https://domotify.net/api/temperatura/getTemp.php";
            String query = String.format("houseId=%d", houseId);
            urlString += "?" + query;

            URL url = new URL(urlString);

            // Set up the HTTP connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read the server's response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();

            // Close the connection and reader
            reader.close();
            connection.disconnect();

            // Parse the server's response
            JSONObject jsonResponse = new JSONObject(response);
            String hasFridge = jsonResponse.getString("hasTemp");

            if (hasFridge.equals("1")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private int checkTempActual(int houseId) {
        int tempActual = 0;

        try {
            // Build the URL of the HTTP GET request
            String urlString = "https://domotify.net/api/temperatura/getTemp.php";
            String query = String.format("houseId=%d", houseId);
            urlString += "?" + query;

            URL url = new URL(urlString);

            // Set up the HTTP connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read the server's response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();

            // Close the connection and reader
            reader.close();
            connection.disconnect();

            // Parse the server's response
            JSONObject jsonResponse = new JSONObject(response);
            tempActual = jsonResponse.getInt("tempActual");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tempActual;
    }


    private boolean checkHasGarage(int houseId) {
        try {
            // Build the URL of the HTTP GET request
            String urlString = "https://domotify.net/api/garage/getGarage.php";
            String query = String.format("houseId=%d", houseId);
            urlString += "?" + query;

            URL url = new URL(urlString);

            // Set up the HTTP connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read the server's response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();

            // Close the connection and reader
            reader.close();
            connection.disconnect();

            // Parse the server's response
            JSONObject jsonResponse = new JSONObject(response);
            String hasGarage = jsonResponse.getString("hasGarage");

            if (hasGarage.equals("1")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    private boolean checkHasCam(int houseId) {
        try {
            // Build the URL of the HTTP GET request
            String urlString = "https://domotify.net/api/cam/getCam.php";
            String query = String.format("houseId=%d", houseId);
            urlString += "?" + query;

            URL url = new URL(urlString);

            // Set up the HTTP connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read the server's response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();

            // Close the connection and reader
            reader.close();
            connection.disconnect();

            // Parse the server's response
            JSONObject jsonResponse = new JSONObject(response);
            String hasCam = jsonResponse.getString("hasCam");

            if (hasCam.equals("1")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    private boolean checkHasLed(int houseId) {
        try {
            // Build the URL of the HTTP GET request
            String urlString = "https://domotify.net/api/led/getLed.php";
            String query = String.format("houseId=%d", houseId);
            urlString += "?" + query;

            URL url = new URL(urlString);

            // Set up the HTTP connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read the server's response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = reader.readLine();

            // Close the connection and reader
            reader.close();
            connection.disconnect();

            // Parse the server's response
            JSONObject jsonResponse = new JSONObject(response);
            String hasLed = jsonResponse.getString("hasLed");

            if (hasLed.equals("1")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    public void openAdminDashboard(int houseId, String username) {


        // Crear el panel y los botones
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel panel = new JPanel(new GridLayout(3, 3));  // Nuevo LayoutManager

        // ICONOS
        ImageIcon fridgeIcon = new ImageIcon("src/main/java/img/fridgeIcon.png");
        ImageIcon garageIcon = new ImageIcon("src/main/java/img/garageIcon.png");
        ImageIcon ledIcon = new ImageIcon("src/main/java/img/ledIcon.png");
        ImageIcon camIcon = new ImageIcon("src/main/java/img/camIcon.png");
        ImageIcon manageHotIcon = new ImageIcon("src/main/java/img/hotIcon.png");
        ImageIcon manageBZZIcon = new ImageIcon("src/main/java/img/BZZIcon.png");
        //3mas
        ImageIcon manageMusicaIcon = new ImageIcon("src/main/java/img/MusicaIcon.png");
        ImageIcon manageCuentaIcon = new ImageIcon("src/main/java/img/MiCuentaIcon.png");
        ImageIcon manageSoporteIcon = new ImageIcon("src/main/java/img/soporte.png");

        JButton manageFridgeButton = new JButton("Gestionar Frigorífico");
        manageFridgeButton.setIcon(fridgeIcon);  // Añadir ícono al botón

        JButton manageGarageButton = new JButton("Gestionar Garaje");
        manageGarageButton.setIcon(garageIcon);  // Añadir ícono al botón

        JButton manageLedButton = new JButton("Gestionar Luces LED");
        manageLedButton.setIcon(ledIcon);  // Añadir ícono al botón

        JButton manageCamButton = new JButton("Gestionar Seguridad");
        manageCamButton.setIcon(camIcon);  // Añadir ícono al botón

        JButton manageHot = new JButton("Gestionar Temperatura");
        manageHot.setIcon(manageHotIcon);  // Añadir ícono al botón

        JButton manageBZZ = new JButton("Gestionar Anti-Mosquitos");
        manageBZZ.setIcon(manageBZZIcon);  // Añadir ícono al botón

        JButton manageMusica = new JButton("Gestionar Musica");
        manageMusica.setIcon(manageMusicaIcon);  // Añadir ícono al botón

        JButton manageCuenta = new JButton("Gestionar Mi Cuenta");
        manageCuenta.setIcon(manageCuentaIcon);  // Añadir ícono al botón

        JButton manageBotonSoporte = new JButton("Soporte Tecnico");
        manageBotonSoporte.setIcon(manageSoporteIcon);  // Añadir ícono al botón

        // Añadir los botones al panel
        panel.add(manageFridgeButton);
        panel.add(manageGarageButton);
        panel.add(manageLedButton);
        panel.add(manageCamButton);
        panel.add(manageHot);
        panel.add(manageBZZ);
        panel.add(manageMusica);
        panel.add(manageCuenta);
        panel.add(manageBotonSoporte);

        manageMusica.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean hasSpoti = checkHasSpoti(houseId);
                if (hasSpoti) {
                   // aqui iria: manageSpotify(houseId); pero como no lo hemos acabado pues ponemos un mensaje de error.
                    // queremos implementar la API de Spotify pero es bastante dificil
                    JOptionPane.showMessageDialog(null, "Error. Matenimiento temporal. Por favor, cualquier incidencia contacta con Domotify.");
                } else {
                    JOptionPane.showMessageDialog(null, "No tienes el Spotify configurado en esta casa.");
                }
            }
        });

        manageCuenta.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                manageAccount(houseId);
            }
        });

        // Agregar comportamiento al clic para cada botón
        manageFridgeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                manageFridge(houseId);
            }
        });

        manageGarageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean hasGarage = checkHasGarage(houseId);
                if (hasGarage) {
                    manageGarage(houseId);
                } else {
                    JOptionPane.showMessageDialog(null, "No hay garaje en esta casa.");
                }
            }
        });

        manageBZZ.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean hasMosquitos = checkHasMosquitos(houseId);
                if (hasMosquitos) {
                    manageMosquitos(houseId);
                } else {
                    JOptionPane.showMessageDialog(null, "No hay anti mosquitos en esta casa.");
                }
            }
        });

        manageLedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean hasLed = checkHasLed(houseId);
                if (hasLed) {
                   manageLed(houseId);
                } else {
                    JOptionPane.showMessageDialog(null, "No hay luces LED en esta casa.");
                }
            }
        });

        manageCamButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean hasCam = checkHasCam(houseId);
                if (hasCam) {
                    manageCam(houseId);
                } else {
                    JOptionPane.showMessageDialog(null, "No hay cámara en esta casa.");
                }
            }
        });

        manageBotonSoporte.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                manageSoporte(1);
            }
        });

        manageHot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean hasTemp = checkHasTemp(houseId);
                int tempActual = checkTempActual(houseId);
                if (hasTemp) {
                    manageTemperature(houseId, String.valueOf(tempActual));
                } else {
                    JOptionPane.showMessageDialog(null, "No hay sistema de temperatura configurado en esta casa.");
                }
            }
        });

        // Crear el marco para la interfaz de usuario
        JFrame frame = new JFrame("Panel de Administracion | Domotify v" + versionDom);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 350);  // Modificar tamaño de ventana para acomodar 3 botones por fila

        // Crear RainbowLabel (reemplazar JLabel)
        RainbowLabel welcomeLabel = new RainbowLabel("¡Bienvenido al panel de control " + username + ", esperemos que disfrutes de tus productos Domotify!", SwingConstants.CENTER);
        welcomeLabel.setOpaque(true);
        welcomeLabel.setBackground(Color.BLACK);
        RainbowLabel infoLabel = new RainbowLabel("Puedes comprar nuestros productos en www.domotify.net", SwingConstants.CENTER);
        infoLabel.setOpaque(true);
        infoLabel.setBackground(Color.BLACK);

        mainPanel.add(welcomeLabel, BorderLayout.NORTH);
        mainPanel.add(panel, BorderLayout.CENTER); // Añade el panel de botones al centro
        mainPanel.add(infoLabel, BorderLayout.SOUTH);

        // Agregar el panel al marco
        frame.add(mainPanel);

        frame.setLocationRelativeTo(null);

        // Mostrar la interfaz de usuario
        frame.setVisible(true);
    }

    // Clase personalizada RainbowLabel
    class RainbowLabel extends JLabel {
        private float hue = 0.0f;

        public RainbowLabel(String text, int alignment) {
            super(text, alignment);

            // Crear un Timer que cambie el color del texto cada 100 milisegundos
            new Timer(100, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Convertir el valor de hue a un color RGB
                    Color color = Color.getHSBColor(hue, 1.0f, 1.0f);
                    // Actualizar el color del texto
                    setForeground(color);
                    // Avanzar el valor de hue, volviendo a 0 cuando alcanza 1
                    hue += 0.01f;
                    if (hue >= 1.0f) {
                        hue = 0.0f;
                    }
                }
            }).start();
        }
    }


    private void manageCam(int houseId) {
        boolean hasCam = checkHasCam(houseId);
        if (!hasCam) {
            JOptionPane.showMessageDialog(null, "No hay camara de videovigilancia en esta casa.");
            return;
        }
        JFrame videoFrame = new JFrame("Videovigilancia");
        videoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        videoFrame.setSize(800, 600);

        // Panel principal
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(800, 750));

        // Foto del profesor arriba en el panel principal
        ImageIcon mainPhotoIcon = new ImageIcon("src/main/java/img/caminas.jpeg");
        Image mainScaledImage = mainPhotoIcon.getImage().getScaledInstance(800, 750, Image.SCALE_SMOOTH);
        ImageIcon mainScaledIcon = new ImageIcon(mainScaledImage);
        JLabel mainPhotoLabel = new JLabel(mainScaledIcon);
        mainPhotoLabel.setBounds(0, 0, 800, 750);
        layeredPane.add(mainPhotoLabel, JLayeredPane.DEFAULT_LAYER);

// Panel central con el texto "Cámara de Vigilancia 001 Pasillo Central Piso 2"
        JLabel camLabel = new JLabel("Cámara de Vigilancia 001 Pasillo Central Piso 2");
        camLabel.setHorizontalAlignment(SwingConstants.CENTER);
        camLabel.setFont(new Font("Arial", Font.BOLD, 20));
        camLabel.setForeground(Color.WHITE);
        camLabel.setBounds(0, 0, 800, 30);
        layeredPane.add(camLabel, JLayeredPane.PALETTE_LAYER);


        // Panel inferior con el texto "Profesor de guardia: Victor Ponz"
        JLabel guardLabel = new JLabel("Profesor de guardia:");
        guardLabel.setHorizontalAlignment(SwingConstants.CENTER);
        guardLabel.setFont(new Font("Arial", Font.BOLD, 16));
        guardLabel.setForeground(Color.WHITE);
        guardLabel.setBounds(0, 550, 800, 20);
        layeredPane.add(guardLabel, JLayeredPane.PALETTE_LAYER);

        // Panel inferior con la foto del profesor y su nombre
        JPanel photoPanel = new JPanel(new BorderLayout());
        photoPanel.setBackground(Color.WHITE);
        photoPanel.setBounds(325, 570, 150, 150);

        ImageIcon photoIcon = new ImageIcon("src/main/java/img/victor.jpg");
        Image scaledImage = photoIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JLabel photoLabel = new JLabel(scaledIcon, SwingConstants.CENTER);
        photoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel nameLabel = new JLabel("Victor Ponz", SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));

        photoPanel.add(photoLabel, BorderLayout.CENTER);
        photoPanel.add(nameLabel, BorderLayout.SOUTH);
        layeredPane.add(photoPanel, JLayeredPane.PALETTE_LAYER);

        videoFrame.getContentPane().add(layeredPane);
        videoFrame.pack();
        videoFrame.setVisible(true);
    }


    private void manageAccount(int houseId) {
        // Crear el panel y los componentes
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setOpaque(false);

        JButton exportButton = new JButton("Exportar datos");

        // Realizar la petición a la API para obtener los datos de la cuenta
        try {
            URL url = new URL("https://domotify.net/api/account/getAccount.php?houseId=" + houseId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Leer respuesta del servidor
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            // Cerrar conexiones
            in.close();
            connection.disconnect();

            // Transformar los datos JSON en un objeto de Java
            JSONObject accountData = new JSONObject(content.toString());

            JLabel userLabel = new JLabel("Usuario: " + accountData.getString("usuario"));
            JLabel fridgeLabel = new JLabel("Frigorifico: " + accountData.getString("frigorifico"));
            JLabel camerasLabel = new JLabel("Camaras: " + accountData.getString("camaras"));

            panel.add(userLabel);
            panel.add(fridgeLabel);
            panel.add(camerasLabel);

        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
        }

        // Agregar comportamiento al clic del botón de exportar
        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try (PrintWriter out = new PrintWriter("datosCuenta.txt")) {

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        panel.add(exportButton);

        // Crear el marco para la interfaz de usuario
        JFrame frame = new JFrame("Gestión de Cuenta");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cerrar solo esta ventana, no toda la aplicación
        frame.setSize(675, 675);

        // Crear un JLayeredPane para permitir la superposición de componentes
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, 675, 675);

        // Cargar imagen de fondo
        ImageIcon backgroundImage = new ImageIcon("src/main/java/img/account.png");
        JLabel backgroundLabel = new JLabel(backgroundImage);
        backgroundLabel.setBounds(0, 0, 675, 675);

        // Añadir la etiqueta de imagen al JLayeredPane en el nivel más bajo
        layeredPane.add(backgroundLabel, Integer.valueOf(0));

        // Ajustar el tamaño y la posición del panel
        panel.setBounds(50, 120, 400, 500); // Tamaño aumentado para acomodar los nuevos elementos

        // Añadir el panel al JLayeredPane en un nivel superior
        layeredPane.add(panel, Integer.valueOf(1));

        // Añadir el JLayeredPane al JFrame
        frame.add(layeredPane);

        frame.setLocationRelativeTo(null);

        // Mostrar la interfaz de usuario
        frame.setVisible(true);
    }

    private void manageLed(int houseId) {
        boolean hasLed = checkHasLed(houseId);
        if (!hasLed) {
            JOptionPane.showMessageDialog(null, "No hay luces LED en esta casa.");
            return;
        }

        // Crear el panel y los componentes
        JPanel panel = new JPanel();
        panel.setOpaque(false);

        JLabel ledNameLabel = new JLabel("Nombre de LED:");
        ledNameLabel.setOpaque(true);
        ledNameLabel.setBackground(Color.BLACK);
        ledNameLabel.setForeground(Color.WHITE);
        JTextField ledNameTextField = new JTextField(10);

        JButton colorChooserButton = new JButton("Elige el color");
        Color[] selectedColor = new Color[1];

        JLabel ledModeLabel = new JLabel("Estado del LED:");
        ledModeLabel.setOpaque(true);
        ledModeLabel.setBackground(Color.BLACK);
        ledModeLabel.setForeground(Color.WHITE);
        String[] ledStates = {"ON", "OFF"};
        JComboBox<String> ledStateComboBox = new JComboBox<>(ledStates);


        JButton saveButton = new JButton("Guardar cambios");

        // Añadir los componentes al panel
        panel.add(ledNameLabel);
        panel.add(ledNameTextField);
        panel.add(colorChooserButton);
        panel.add(ledModeLabel);
        panel.add(ledStateComboBox);
        panel.add(saveButton);

        // Obtener datos existentes de la base de datos
        try {
            URL url = new URL("https://domotify.net/api/led/getLed.php?houseId=" + houseId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            // Cerrar conexiones
            in.close();
            connection.disconnect();

            // Parse response
            JSONObject ledData = new JSONObject(content.toString());

            ledNameTextField.setText(ledData.getString("ledName"));
            int r = ledData.getInt("ledR");
            int g = ledData.getInt("ledG");
            int b = ledData.getInt("ledB");
            selectedColor[0] = new Color(r, g, b);
            colorChooserButton.setBackground(selectedColor[0]);
            String ledStateFromDb = ledData.getString("ledMode");
            ledStateComboBox.setSelectedItem(ledStateFromDb);


        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        // Comportamiento del botón del selector de color
        colorChooserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectedColor[0] = JColorChooser.showDialog(null, "Seleccione un color", selectedColor[0]);
                colorChooserButton.setBackground(selectedColor[0]);
            }
        });

        // Agregar comportamiento al clic del botón de guardar
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String ledName = ledNameTextField.getText();
                String ledR = String.valueOf(selectedColor[0].getRed());
                String ledG = String.valueOf(selectedColor[0].getGreen());
                String ledB = String.valueOf(selectedColor[0].getBlue());
                String ledMode = (String) ledStateComboBox.getSelectedItem();


                // Construir los datos para enviar
                String urlParameters = "houseId=" + houseId + "&hasLed=1" + "&ledName=" + ledName + "&ledR=" + ledR + "&ledG=" + ledG + "&ledB=" + ledB + "&ledMode=" + ledMode;

                // Crear conexión y enviar los datos
                try {
                    URL url = new URL("https://domotify.net/api/led/updateLed.php");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);

                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                    wr.writeBytes(urlParameters);
                    wr.flush();
                    wr.close();

                    // Leer respuesta del servidor
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }

                    // Cerrar conexiones
                    in.close();
                    connection.disconnect();

                    // Manejar respuesta
                    String response = content.toString();
                    if (response.equals("success")) {
                        JOptionPane.showMessageDialog(null, "Cambios guardados con éxito.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Hubo un error al guardar los cambios.");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Crear el marco para la interfaz de usuario
        JFrame frame = new JFrame("Gestión de LEDS");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cerrar solo esta ventana, no toda la aplicación
        frame.setSize(675, 675);

        // Crear un JLayeredPane para permitir la superposición de componentes
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, 675, 675);  // Asegúrate de que este tamaño coincida con el tamaño del JFrame

        // Cargar imagen de fondo
        ImageIcon backgroundImage = new ImageIcon("src/main/java/img/leds.png");
        JLabel backgroundLabel = new JLabel(backgroundImage);
        backgroundLabel.setBounds(0, 0, 675, 675);  // Asegúrate de que este tamaño coincida con el tamaño de la imagen y el JFrame

        // Añadir la etiqueta de imagen al JLayeredPane en el nivel más bajo
        layeredPane.add(backgroundLabel, Integer.valueOf(0));

        // Ajustar el tamaño y la posición del panel
        panel.setBounds(300, 140, 160, 300);  // Puedes ajustar estos valores según sea necesario

        // Añadir el panel al JLayeredPane en un nivel superior
        layeredPane.add(panel, Integer.valueOf(1));

        // Añadir el JLayeredPane al JFrame
        frame.add(layeredPane);

        frame.setLocationRelativeTo(null);

        // Mostrar la interfaz de usuario
        frame.setVisible(true);
    }



    private void manageSpotify(int houseId) {
        boolean hasSpoti = checkHasSpoti(houseId);
        if (!hasSpoti) {
            JOptionPane.showMessageDialog(null, "No hay Spotify configurado en esta casa.");
            return;
        }

    }




    private void manageMosquitos(int houseId) {
        boolean hasMosquitos = checkHasMosquitos(houseId);
        if (!hasMosquitos) {
            JOptionPane.showMessageDialog(null, "No hay anti mosquitos en esta casa.");
            return;
        }

        // Crear el panel y los componentes
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Añadir esto para organizar los elementos verticalmente

        JLabel hab1Label = new JLabel("Habitacion 1:");
        JTextField hab1TextField = new JTextField(4);
        JCheckBox hab1Check = new JCheckBox("Encender/Apagar");

        JLabel hab2Label = new JLabel("Habitacion 2:");
        JTextField hab2TextField = new JTextField(4);
        JCheckBox hab2Check = new JCheckBox("Encender/Apagar");

        JLabel hab3Label = new JLabel("Habitacion 3:");
        JTextField hab3TextField = new JTextField(4);
        JCheckBox hab3Check = new JCheckBox("Encender/Apagar");

        JCheckBox salonCheck = new JCheckBox("Encender/Apagar Salon");
        JCheckBox cocinaCheck = new JCheckBox("Encender/Apagar Cocina");
        JCheckBox banoCheck = new JCheckBox("Encender/Apagar Baño");
        JCheckBox planta1Check = new JCheckBox("Encender/Apagar Planta 1");
        JCheckBox planta2Check = new JCheckBox("Encender/Apagar Planta 2");

        JButton saveButton = new JButton("Guardar cambios");

        // Añadir los componentes al panel
        panel.add(hab1Label);
        panel.add(hab1TextField);
        panel.add(hab1Check);
        panel.add(hab2Label);
        panel.add(hab2TextField);
        panel.add(hab2Check);
        panel.add(hab3Label);
        panel.add(hab3TextField);
        panel.add(hab3Check);
        panel.add(salonCheck);
        panel.add(cocinaCheck);
        panel.add(banoCheck);
        panel.add(planta1Check);
        panel.add(planta2Check);
        panel.add(saveButton);

        // Agregar comportamiento al clic del botón de guardar
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String nameHab1 = hab1TextField.getText();
                String nameHab2 = hab2TextField.getText();
                String nameHab3 = hab3TextField.getText();
                int hab1 = hab1Check.isSelected() ? 1 : 0;
                int hab2 = hab2Check.isSelected() ? 1 : 0;
                int hab3 = hab3Check.isSelected() ? 1 : 0;
                int salon = salonCheck.isSelected() ? 1 : 0;
                int cocina = cocinaCheck.isSelected() ? 1 : 0;
                int bano = banoCheck.isSelected() ? 1 : 0;
                int planta1 = planta1Check.isSelected() ? 1 : 0;
                int planta2 = planta2Check.isSelected() ? 1 : 0;

                // Construir los datos para enviar
                String urlParameters = "idMosquito=" + houseId +
                        "&houseId=" + houseId +
                        "&hasMosquito=1" +
                        "&NameHab1=" + nameHab1 +
                        "&NameHab2=" + nameHab2 +
                        "&NameHab3=" + nameHab3 +
                        "&Habitacion1=" + hab1 +
                        "&Habitacion2=" + hab2 +
                        "&Habitacion3=" + hab3 +
                        "&Salon=" + salon +
                        "&Cocina=" + cocina +
                        "&Baño=" + bano +
                        "&Planta1=" + planta1 +
                        "&Planta2=" + planta2 +
                        "&modeMosquito=active";


                // Crear conexión y enviar los datos
                try {
                    URL url = new URL("https://domotify.net/api/mosquitos/updateMosquito.php");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);

                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                    wr.writeBytes(urlParameters);
                    wr.flush();
                    wr.close();

                    // Leer respuesta del servidor
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }

                    // Cerrar conexiones
                    in.close();
                    connection.disconnect();

                    // Manejar respuesta
                    String response = content.toString();
                    if (response.equals("ok")) {
                        JOptionPane.showMessageDialog(null, "Cambios guardados con éxito.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Hubo un error al guardar los cambios.");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        try {
            URL url = new URL("https://domotify.net/api/mosquitos/getMosquito.php?houseId=" + houseId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Leer respuesta del servidor
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            // Cerrar conexiones
            in.close();
            connection.disconnect();

            // Parsear respuesta a JSON
            JSONObject json = new JSONObject(content.toString());

            // Asignar los valores existentes a los campos de texto
            hab1TextField.setText(json.getString("NameHab1"));
            hab2TextField.setText(json.getString("NameHab2"));
            hab3TextField.setText(json.getString("NameHab3"));
            hab1Check.setSelected(json.getInt("Habitacion1") == 1);
            hab2Check.setSelected(json.getInt("Habitacion2") == 1);
            hab3Check.setSelected(json.getInt("Habitacion3") == 1);
            salonCheck.setSelected(json.getInt("Salon") == 1);
            cocinaCheck.setSelected(json.getInt("Cocina") == 1);
            banoCheck.setSelected(json.getInt("Baño") == 1);
            planta1Check.setSelected(json.getInt("Planta1") == 1);
            planta2Check.setSelected(json.getInt("Planta2") == 1);

        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
        }

        // Crear el marco para la interfaz de usuario
        JFrame frame = new JFrame("Gestión de Anti Mosquitos");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cerrar solo esta ventana, no toda la aplicación
        frame.setSize(675, 675);

        // Crear un JLayeredPane para permitir la superposición de componentes
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, 675, 675);

        // Cargar imagen de fondo
        ImageIcon backgroundImage = new ImageIcon("src/main/java/img/mosquitos.png");
        JLabel backgroundLabel = new JLabel(backgroundImage);
        backgroundLabel.setBounds(0, 0, 675, 675);

        // Añadir la etiqueta de imagen al JLayeredPane en el nivel más bajo
        layeredPane.add(backgroundLabel, Integer.valueOf(0));

        // Ajustar el tamaño y la posición del panel
        panel.setBounds(30, 75, 520, 420); // Tamaño aumentado para acomodar los nuevos elementos

        // Añadir el panel al JLayeredPane en un nivel superior
        layeredPane.add(panel, Integer.valueOf(1));

        // Añadir el JLayeredPane al JFrame
        frame.add(layeredPane);

        frame.setLocationRelativeTo(null);

        // Mostrar la interfaz de usuario
        frame.setVisible(true);
    }




    private void manageFridge(int houseId) {
        boolean hasFridge = checkHasFridge(houseId);
        if (!hasFridge) {
            JOptionPane.showMessageDialog(null, "No hay frigorífico en esta casa.");
            return;
        }

        // Crear el panel y los componentes
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        JLabel fridgeNameLabel = new JLabel("Nombre del frigorífico:");
        fridgeNameLabel.setOpaque(true);
        fridgeNameLabel.setBackground(Color.BLACK);
        fridgeNameLabel.setForeground(Color.WHITE);
        JTextField fridgeNameTextField = new JTextField(10);
        JLabel fridgeTempLabel = new JLabel("Temperatura del frigorífico:");
        fridgeTempLabel.setOpaque(true);
        fridgeTempLabel.setForeground(Color.WHITE);
        fridgeTempLabel.setBackground(Color.BLACK);
        JTextField fridgeTempTextField = new JTextField(10);
        JLabel fridgeModeLabel = new JLabel("Modo del frigorífico:");
        fridgeModeLabel.setOpaque(true);
        fridgeModeLabel.setForeground(Color.WHITE);
        fridgeModeLabel.setBackground(Color.BLACK);
        String[] fridgeModes = { "Eco", "Normal", "Boost" };
        JComboBox<String> fridgeModeComboBox = new JComboBox<>(fridgeModes);
        JButton saveButton = new JButton("Guardar cambios");



        // Añadir los componentes al panel
        panel.add(fridgeNameLabel);
        panel.add(fridgeNameTextField);
        panel.add(fridgeTempLabel);
        panel.add(fridgeTempTextField);
        panel.add(fridgeModeLabel);
        panel.add(fridgeModeComboBox);
        panel.add(saveButton);

        // Agregar comportamiento al clic del botón de guardar
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String fridgeName = fridgeNameTextField.getText();
                String fridgeTemp = fridgeTempTextField.getText();
                String fridgeMode = (String) fridgeModeComboBox.getSelectedItem();

                // Construir los datos para enviar
                String urlParameters = "houseId=" + houseId + "&hasFridge=1" + "&fridgeName=" + fridgeName + "&fridgeTemp=" + fridgeTemp + "&fridgeMode=" + fridgeMode;

                // Crear conexión y enviar los datos
                try {
                    URL url = new URL("https://domotify.net/api/fridge/updateFridge.php");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);

                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                    wr.writeBytes(urlParameters);
                    wr.flush();
                    wr.close();

                    // Leer respuesta del servidor
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }

                    // Cerrar conexiones
                    in.close();
                    connection.disconnect();

                    // Manejar respuesta
                    String response = content.toString();
                    if (response.equals("success")) {
                        JOptionPane.showMessageDialog(null, "Cambios guardados con éxito.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Hubo un error al guardar los cambios.");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Crear el marco para la interfaz de usuario
        // Crear el marco para la interfaz de usuario
        JFrame frame = new JFrame("Gestión de frigorífico");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cerrar solo esta ventana, no toda la aplicación
        frame.setSize(675, 675);

        // Crear un JLayeredPane para permitir la superposición de componentes
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, 675, 675);  // Asegúrate de que este tamaño coincida con el tamaño del JFrame

        // Cargar imagen de fondo
        ImageIcon backgroundImage = new ImageIcon("src/main/java/img/nevera.png");
        JLabel backgroundLabel = new JLabel(backgroundImage);
        backgroundLabel.setBounds(0, 0, 675, 675);  // Asegúrate de que este tamaño coincida con el tamaño de la imagen y el JFrame

        // Añadir la etiqueta de imagen al JLayeredPane en el nivel más bajo
        layeredPane.add(backgroundLabel, Integer.valueOf(0));

        // Ajustar el tamaño y la posición del panel
        panel.setBounds(100, 100, 230, 160);  // Puedes ajustar estos valores según sea necesario

        // Añadir el panel al JLayeredPane en un nivel superior
        layeredPane.add(panel, Integer.valueOf(1));

        // Añadir el JLayeredPane al JFrame
        frame.add(layeredPane);

        frame.setLocationRelativeTo(null);

        // Mostrar la interfaz de usuario
        frame.setVisible(true);
    }

    private void manageTemperature(int houseId, String tempActual) {
        // Crear el panel y los componentes
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        JLabel tempActualLabel = new JLabel("Temperatura Actual:");
        JTextField tempActualTextField = new JTextField(tempActual, 10);
        JLabel modeTempLabel = new JLabel("Modo de temperatura:");

        tempActualLabel.setForeground(Color.WHITE);
        tempActualLabel.setOpaque(true);
        tempActualLabel.setBackground(Color.BLACK);


        modeTempLabel.setForeground(Color.WHITE);
        modeTempLabel.setOpaque(true);
        modeTempLabel.setBackground(Color.BLACK);

        String[] modeTemps = { "Apagado", "Low", "Medium", "Boost" };
        JComboBox<String> modeTempComboBox = new JComboBox<>(modeTemps);
        JButton saveButton = new JButton("Guardar cambios");

        // Añadir los componentes al panel
        panel.add(tempActualLabel);
        panel.add(tempActualTextField);
        panel.add(modeTempLabel);
        panel.add(modeTempComboBox);
        panel.add(saveButton);



        // Crear el marco para la interfaz de usuario
        final JFrame frame = new JFrame("Gestión de temperatura");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cerrar solo esta ventana, no toda la aplicación
        frame.setSize(675, 675);

        // Crear un JLayeredPane para permitir la superposición de componentes
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, 675, 675);  // Asegúrate de que este tamaño coincida con el tamaño del JFrame

        // Cargar imagen de fondo
        ImageIcon backgroundImage = new ImageIcon("src/main/java/img/temperatura" + tempActual + ".png");
        JLabel backgroundLabel = new JLabel(backgroundImage);
        backgroundLabel.setBounds(0, 0, 675, 675);  // Asegúrate de que este tamaño coincida con el tamaño de la imagen y el JFrame

        // Añadir la etiqueta de imagen al JLayeredPane en el nivel más bajo
        layeredPane.add(backgroundLabel, Integer.valueOf(0));

        // Ajustar el tamaño y la posición del panel
        panel.setBounds(94, 140, 200, 130);  // Puedes ajustar estos valores según sea necesario

        // Añadir el panel al JLayeredPane en un nivel superior
        layeredPane.add(panel, Integer.valueOf(1));

        // Añadir el JLayeredPane al JFrame
        frame.add(layeredPane);

        frame.setLocationRelativeTo(null);

        // Mostrar la interfaz de usuario
        frame.setVisible(true);

        // Agregar comportamiento al clic del botón de guardar
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String newTempActual = tempActualTextField.getText();
                String modeTemp = (String) modeTempComboBox.getSelectedItem();

                // Construir los datos para enviar
                String urlParameters = "houseId=" + houseId + "&hasTemp=1" + "&tempActual=" + newTempActual + "&modeTemp=" + modeTemp;

                // Crear conexión y enviar los datos
                try {
                    URL url = new URL("https://domotify.net/api/temperatura/updateTemp.php");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);

                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                    wr.writeBytes(urlParameters);
                    wr.flush();
                    wr.close();

                    // Leer respuesta del servidor
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }

                    // Cerrar conexiones
                    in.close();
                    connection.disconnect();

                    // Manejar respuesta
                    String response = content.toString();
                    if (response.equals("success")) {
                        JOptionPane.showMessageDialog(null, "Cambios guardados con éxito.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Hubo un error al guardar los cambios.");
                    }

                    frame.dispose();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

    }



    // FALTA POR ACABAR !!
    private void manageSoporte(int houseId) {

        String ACCOUNT_SID = "";
        String AUTH_TOKEN = "";

        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);


        String phoneNumber = JOptionPane.showInputDialog("Por favor, introduce el número de teléfono con el simbolo + y el prefijo del pais. Ejemplo: +34698905854");

        PhoneNumber to = new PhoneNumber(phoneNumber);
        PhoneNumber from = new PhoneNumber("+34964800212");
        URI uri = null;

        try {
            uri = new URI("http://74.208.82.130:8888/voice");
        } catch (URISyntaxException e) {
            System.err.println("Invalid URI");
            e.printStackTrace();
            return;
        }

        Call call = Call.creator(to, from, uri).create();

        System.out.println(call.getSid());
    }

    private void manageGarage(int houseId) {
        boolean hasGarage = checkHasGarage(houseId);
        if (!hasGarage) {
            JOptionPane.showMessageDialog(null, "No hay garage en esta casa.");
            return;
        }

        // Crear el panel y los componentes
        JPanel panel = new JPanel();
        JLabel garageTextMin = new JLabel("Temporizador garaje en minutos:");
        JSlider garageTempSlider = new JSlider(JSlider.HORIZONTAL, 0, 60, 0);
        JLabel garageModeLabel = new JLabel("Modo del garaje:");
        String[] garageModes = { "Abrir ahora", "Cerrar ahora", "Abrir con Temporizador", "Cerrar con Temporizador" };
        JComboBox<String> garageModeComboBox = new JComboBox<>(garageModes);
        JButton saveButton = new JButton("Guardar cambios");


        //config-slider
        garageTempSlider.setMajorTickSpacing(20);  // configurar los intervalos de las marcas grandes
        garageTempSlider.setMinorTickSpacing(5);   // configurar los intervalos de las marcas pequeñas
        garageTempSlider.setPaintTicks(true);      // pintar las marcas
        garageTempSlider.setPaintLabels(true);     // pintar las etiquetas de los valores


        // Añadir los componentes al panel
        panel.add(garageTextMin);
        panel.add(garageTempSlider);
        panel.add(garageModeLabel);
        panel.add(garageModeComboBox);
        panel.add(saveButton);

        // Agregar comportamiento al clic del botón de guardar
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int garageTemp = garageTempSlider.getValue();
                String garageMode = (String) garageModeComboBox.getSelectedItem();

                // Construir los datos para enviar
                String urlParameters = "houseId=" + houseId + "&hasGarage=1" + "&garageTemp=" + garageTemp + "&modeGarage=" + garageMode;

                // Crear conexión y enviar los datos
                try {
                    URL url = new URL("https://domotify.net/api/garage/updateGarage.php");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);

                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                    wr.writeBytes(urlParameters);
                    wr.flush();
                    wr.close();

                    // Leer respuesta del servidor
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }

                    // Cerrar conexiones
                    in.close();
                    connection.disconnect();

                    // Manejar respuesta
                    String response = content.toString();
                    if (response.equals("success")) {
                        JOptionPane.showMessageDialog(null, "Cambios guardados con éxito.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Hubo un error al guardar los cambios.");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Crear el marco para la interfaz de usuario
        // Crear el marco para la interfaz de usuario
        JFrame frame = new JFrame("Gestión de Garaje");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cerrar solo esta ventana, no toda la aplicación
        frame.setSize(675, 675);

        // Crear un JLayeredPane para permitir la superposición de componentes
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, 675, 675);

        // Cargar imagen de fondo
        ImageIcon backgroundImage = new ImageIcon("src/main/java/img/garaje.png");
        JLabel backgroundLabel = new JLabel(backgroundImage);
        backgroundLabel.setBounds(0, 0, 675, 675);

        // Añadir la etiqueta de imagen al JLayeredPane en el nivel más bajo
        layeredPane.add(backgroundLabel, Integer.valueOf(0));

        // Ajustar el tamaño y la posición del panel
        panel.setBounds(50, 50, 230, 160);

        // Añadir el panel al JLayeredPane en un nivel superior
        layeredPane.add(panel, Integer.valueOf(1));

        // Añadir el JLayeredPane al JFrame
        frame.add(layeredPane);

        frame.setLocationRelativeTo(null);

        // Mostrar la interfaz de usuario
        frame.setVisible(true);
    }

    private void mensajeLlamada(int houseId, String username) {
        String msg = "¡Hola " + username + "!\nLamentamos informarte que tu cuenta no dispone de ningun producto ACTIVADO de Domotify.\nEn este caso tienes que ponerte en contacto con nuestro equipo de atencion al cliente para que podamos activarte los productos y configurarlos correctamente.\n\nAtencion al cliente 24h / Urgencias:\ninfo@domotify.net | +34698905854 | domotify.net";


        JDialog dialog = new JDialog();
        dialog.setTitle("Tu cuenta no esta configurada. Por favor, contactanos. | Domotify v" + versionDom);
        dialog.setLayout(new BorderLayout());
        dialog.setModal(true);


        JLabel label = new JLabel("<html>" + msg.replace("\n", "<br>") + "</html>");
        label.setHorizontalAlignment(JLabel.CENTER);
        dialog.add(label, BorderLayout.CENTER);

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("Ok");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        buttonPanel.add(okButton);

        JButton callButton = new JButton("Solicitar llamada de Domotify");
        callButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manageSoporte(houseId);
                dialog.dispose();
            }
        });
        buttonPanel.add(callButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }



    //ESTO SERIA PARA NIÑOS PERO NO DA TIEMPO A ACABARLO - EN FASE DE DESARROLLO..
    private void openChildDashboard() {
        mainFrame = new JFrame();

        // Configurar la ventana de niño
        mainFrame.dispose();

        JFrame childFrame = new JFrame("Panel de Niño");
        childFrame.setBounds(100, 100, 400, 300);
        childFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        childFrame.getContentPane().setLayout(new FlowLayout());
        childFrame.setLocationRelativeTo(null);

        JLabel lblChild = new JLabel("¡Bienvenido, pequeño!");
        childFrame.getContentPane().add(lblChild);

        childFrame.setVisible(true);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new APPMain();
            }
        });
    }
}