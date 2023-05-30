import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
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
                boolean hasFridge = checkHasFridge(houseId);
                if (hasFridge) {
                    // Código para manejar el frigorífico aquí
                } else {
                    JOptionPane.showMessageDialog(null, "No hay frigorífico en esta casa.");
                }
            }
        });

        manageGarageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean hasGarage = checkHasGarage(houseId);
                if (hasGarage) {
                    // Código para manejar el garaje aquí
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
        JFrame frame = new JFrame("Admin Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        // Agregar el panel al marco
        frame.add(panel);

        // Mostrar la interfaz de usuario
        frame.setVisible(true);
    }

    private void manageFridge(int houseId) {
        // Este es el lugar donde puedes abrir una nueva ventana o panel para gestionar el frigorífico.
        // También puedes llamar a las APIs de PHP desde aquí para gestionar el frigorífico.
        // Esta es sólo una función de marcador de posición. Deberías implementarla según tus necesidades.
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
