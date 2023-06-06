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
                openAdminDashboard(ownerHouse);
            } else {
                openChildDashboard();
            }
        } else {
            String msg = "¡Hola " + username + "!\nLamentamos informarte que tu cuenta no dispone de ningun producto ACTIVADO de Domotify.\nEn este caso tienes que ponerte en contacto con nuestro equipo de atencion al cliente para que podamos activarte los productos y configurarlos correctamente.\n\nAtencion al cliente 24h / Urgencias:\ninfo@domotify.net | +34698905854 | domotify.net";
            JOptionPane.showMessageDialog(null, msg);
        }
    }
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
    public void openAdminDashboard(int houseId) {

        // Crear el panel y los botones
        JPanel panel = new JPanel();
        JButton manageFridgeButton = new JButton("Gestionar Frigorífico");
        JButton manageGarageButton = new JButton("Gestionar Garaje");
        JButton manageLedButton = new JButton("Gestionar Luces LED");
        JButton manageCamButton = new JButton("Gestionar Cámara");

        // Añadir los botones al panel
        panel.add(manageFridgeButton);
        panel.add(manageGarageButton);
        panel.add(manageLedButton);
        panel.add(manageCamButton);

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

        // Crear el marco para la interfaz de usuario
        JFrame frame = new JFrame("Panel de Administracion | Domotify v1.2");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        // Agregar el panel al marco
        frame.add(panel);

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
        JLabel fridgeNameLabel = new JLabel("Nombre del frigorífico:");
        JTextField fridgeNameTextField = new JTextField(10);
        JLabel fridgeTempLabel = new JLabel("Temperatura del frigorífico:");
        JTextField fridgeTempTextField = new JTextField(10);
        JLabel fridgeModeLabel = new JLabel("Modo del frigorífico:");
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
        panel.setBounds(50, 50, 230, 160);  // Puedes ajustar estos valores según sea necesario

        // Añadir el panel al JLayeredPane en un nivel superior
        layeredPane.add(panel, Integer.valueOf(1));

        // Añadir el JLayeredPane al JFrame
        frame.add(layeredPane);

        // Mostrar la interfaz de usuario
        frame.setVisible(true);
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
