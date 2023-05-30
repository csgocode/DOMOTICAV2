import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
//commit
public class APPMain {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JFrame mainFrame;

    public APPMain() {
        // Configurar la ventana principal
        JFrame frame = new JFrame("Control Domótico");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(createLoginPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
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
                    openDomoticsManagementWindow(isAdmin);
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
            String urlString = "http://domotify.me/api/register.php";
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
            String urlString = "http://domotify.me/api/login.php";
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
            String urlString = "http://domotify.me/api/check_admin.php";
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

    private void openDomoticsManagementWindow(boolean isAdmin) {
        if (isAdmin) {
            openAdminDashboard();
        } else {
            openChildDashboard();
        }
    }

    private void openAdminDashboard() {
        mainFrame = new JFrame();

        // Configurar la ventana de administrador
        mainFrame.dispose();

        JFrame adminFrame = new JFrame("Panel de Administrador");
        adminFrame.setBounds(100, 100, 400, 300);
        adminFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        adminFrame.getContentPane().setLayout(new FlowLayout());
        adminFrame.setLocationRelativeTo(null);

        JLabel lblAdmin = new JLabel("¡Bienvenido, Administrador!");
        adminFrame.getContentPane().add(lblAdmin);

        adminFrame.setVisible(true);
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
