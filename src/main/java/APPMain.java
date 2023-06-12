import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONObject;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;

import java.net.URI;
import java.net.URISyntaxException;

// GIT profe: al361883




//commit
public class APPMain {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JFrame mainFrame;

    public APPMain() {
        // Configurar la ventana principal
        JFrame frame = new JFrame("DOMOTIFY v1.2: Transformando hogares, conectando vidas.");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(createLoginPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.getRootPane().setDefaultButton(loginButton); // Mover esta línea aquí
        frame.setVisible(true);
    }

    //commit prueba

    private JPanel createLoginPanel() {
        JPanel loginPanel = new JPanel(new BorderLayout());

        // Cargar imagen de fondo
        ImageIcon backgroundImage = new ImageIcon("src/main/java/img/login.png");
        JLabel backgroundLabel = new JLabel(backgroundImage);
        loginPanel.add(backgroundLabel, BorderLayout.CENTER);
        backgroundLabel.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        JLabel usernameLabel = new JLabel("USUARIO O CORREO DE DOMOTIFY");
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
            String msg = "¡Hola " + username + "!\nLamentamos informarte que tu cuenta no dispone de ningun producto ACTIVADO de Domotify.\nEn este caso tienes que ponerte en contacto con nuestro equipo de atencion al cliente para que podamos activarte los productos y configurarlos correctamente.\n\nAtencion al cliente 24h / Urgencias:\ninfo@domotify.net | +34698905854 | domotify.net";
            JOptionPane.showMessageDialog(null, msg);
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
        ImageIcon manageRoombaIcon = new ImageIcon("src/main/java/img/roombaIcon.png");
        ImageIcon manageAltavocesIcon = new ImageIcon("src/main/java/img/altavocesIcon.png");
        ImageIcon manageCocinaIcon = new ImageIcon("src/main/java/img/soporte.png");

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

        JButton manageRoomba = new JButton("Gestionar Roomba");
        manageRoomba.setIcon(manageRoombaIcon);  // Añadir ícono al botón

        JButton manageAltavoces = new JButton("Gestionar Altavoces");
        manageAltavoces.setIcon(manageAltavocesIcon);  // Añadir ícono al botón

        JButton manageCocina = new JButton("Soporte Tecnico");
        manageCocina.setIcon(manageCocinaIcon);  // Añadir ícono al botón

        // Añadir los botones al panel
        panel.add(manageFridgeButton);
        panel.add(manageGarageButton);
        panel.add(manageLedButton);
        panel.add(manageCamButton);
        panel.add(manageHot);
        panel.add(manageBZZ);
        panel.add(manageRoomba);
        panel.add(manageAltavoces);
        panel.add(manageCocina);

        // Agregar comportamiento al clic para cada botón
        manageFridgeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                manageFridge(houseId);
            }
        });

        manageLedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                manageLed(houseId);
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

        manageLedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean hasLed = checkHasLed(houseId);
                if (hasLed) {
                    // Código para manejar las luces LED aquí
                } else {
                    JOptionPane.showMessageDialog(null, "No hay luces LED en esta casa.");
                }
            }
        });

        manageCamButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean hasCam = checkHasCam(houseId);
                if (hasCam) {
                    // Código para manejar la cámara aquí
                } else {
                    JOptionPane.showMessageDialog(null, "No hay cámara en esta casa.");
                }
            }
        });

        manageCocina.addActionListener(new ActionListener() {
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
        JFrame frame = new JFrame("Panel de Administracion | Domotify v1.2");
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

    private void manageLed(int houseId) {
        boolean hasLed = checkHasLed(houseId);
        if (!hasLed) {
            JOptionPane.showMessageDialog(null, "No hay luces LED en esta casa.");
            return;
        }

        // Crear el panel y los componentes
        JPanel panel = new JPanel();
        panel.setOpaque(false);

        JLabel ledNameLabel = new JLabel("Nombre del frigorífico:");
        ledNameLabel.setOpaque(true);
        ledNameLabel.setBackground(Color.BLACK);
        ledNameLabel.setForeground(Color.WHITE);
        JTextField ledNameTextField = new JTextField(10);

        JLabel ledRLabel = new JLabel("Nombre del frigorífico:");
        ledRLabel.setOpaque(true);
        ledRLabel.setBackground(Color.BLACK);
        ledRLabel.setForeground(Color.WHITE);
        JTextField ledRTextField = new JTextField(10);

        JLabel ledGLabel = new JLabel("Nombre del frigorífico:");
        ledGLabel.setOpaque(true);
        ledGLabel.setBackground(Color.BLACK);
        ledGLabel.setForeground(Color.WHITE);
        JTextField ledGTextField = new JTextField(10);

        JLabel ledBLabel = new JLabel("Nombre del frigorífico:");
        ledBLabel.setOpaque(true);
        ledBLabel.setBackground(Color.BLACK);
        ledBLabel.setForeground(Color.WHITE);
        JTextField ledBTextField = new JTextField(10);

        JLabel ledModeLabel = new JLabel("Nombre del frigorífico:");
        ledModeLabel.setOpaque(true);
        ledModeLabel.setBackground(Color.BLACK);
        ledModeLabel.setForeground(Color.WHITE);
        JTextField ledModeTextField = new JTextField(10);
        JButton saveButton = new JButton("Guardar cambios");



        // Añadir los componentes al panel
        panel.add(ledNameLabel);
        panel.add(ledNameTextField);
        panel.add(ledRLabel);
        panel.add(ledRTextField);
        panel.add(ledGLabel);
        panel.add(ledGTextField);
        panel.add(ledBLabel);
        panel.add(ledBTextField);
        panel.add(ledModeLabel);
        panel.add(ledModeTextField);
        panel.add(saveButton);

        // Agregar comportamiento al clic del botón de guardar
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String ledName = ledNameTextField.getText();
                String ledR = ledRTextField.getText();
                String ledG = ledGTextField.getText();
                String ledB = ledBTextField.getText();
                String ledMode = ledModeTextField.getText();



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
        panel.setBounds(100, 100, 230, 160);  // Puedes ajustar estos valores según sea necesario

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
        // Tus credenciales de Twilio
        String ACCOUNT_SID = "";
        String AUTH_TOKEN = "";

        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);


        String phoneNumber = JOptionPane.showInputDialog("Por favor, introduce el número de teléfono con el simbolo + y el prefijo del pais. Ejemplo: +34698905854");

        PhoneNumber to = new PhoneNumber(phoneNumber);
        PhoneNumber from = new PhoneNumber("+34611703775");
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
        JSlider garageTempSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        JLabel garageModeLabel = new JLabel("Modo del garaje:");
        String[] garageModes = { "Abierto", "Cerrado", "Custom" };
        JComboBox<String> garageModeComboBox = new JComboBox<>(garageModes);
        JButton saveButton = new JButton("Guardar cambios");


        //config-slider
        garageTempSlider.setMajorTickSpacing(20);  // configurar los intervalos de las marcas grandes
        garageTempSlider.setMinorTickSpacing(5);   // configurar los intervalos de las marcas pequeñas
        garageTempSlider.setPaintTicks(true);      // pintar las marcas
        garageTempSlider.setPaintLabels(true);     // pintar las etiquetas de los valores


        // Añadir los componentes al panel
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
        layeredPane.setBounds(0, 0, 675, 675);  // Asegúrate de que este tamaño coincida con el tamaño del JFrame

        // Cargar imagen de fondo
        ImageIcon backgroundImage = new ImageIcon("src/main/java/img/garaje.png");
        JLabel backgroundLabel = new JLabel(backgroundImage);
        backgroundLabel.setBounds(0, 0, 675, 675);  // Asegúrate de que este tamaño coincida con el tamaño de la imagen y el JFrame

        // Añadir la etiqueta de imagen al JLayeredPane en el nivel más bajo
        layeredPane.add(backgroundLabel, Integer.valueOf(0));

        // Ajustar el tamaño y la posición del panel
        panel.setBounds(50, 50, 230, 160);  // Puedes ajustar estos valores según sea necesario

        // Añadir el panel al JLayeredPane en un nivel superior
        layeredPane.add(panel, Integer.valueOf(1));

        // Añadir el JLayeredPane al JFrame
        frame.add(layeredPane);

        frame.setLocationRelativeTo(null);

        // Mostrar la interfaz de usuario
        frame.setVisible(true);
    }

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
