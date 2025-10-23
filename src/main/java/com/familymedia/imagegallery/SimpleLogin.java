package com.familymedia.imagegallery;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SimpleLogin extends JFrame {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    
    public SimpleLogin() {
        setupUI();
    }
    
    private void setupUI() {
        setTitle("Family Gallery - Login");
        setSize(500, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(63, 81, 181));
        headerPanel.setPreferredSize(new Dimension(500, 180));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        JLabel titleLabel = new JLabel("📷 Family Image Gallery");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Secure & Private Family Photos");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(200, 210, 255));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(Box.createVerticalGlue());
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        headerPanel.add(subtitleLabel);
        headerPanel.add(Box.createVerticalGlue());
        
        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 20, 60));
        
        // Username
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        userLabel.setForeground(new Color(50, 50, 50));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Password
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Arial", Font.BOLD, 14));
        passLabel.setForeground(new Color(50, 50, 50));
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Login Button
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(new Color(63, 81, 181));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(e -> performLogin());
        
        // Hover effect
        loginButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(48, 63, 159));
            }
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(new Color(63, 81, 181));
            }
        });
        
        // Enter key support
        passwordField.addActionListener(e -> performLogin());
        
        formPanel.add(userLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        formPanel.add(usernameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(passLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        formPanel.add(passwordField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        formPanel.add(loginButton);
        
        // Demo Credentials Panel
        JPanel demoPanel = new JPanel();
        demoPanel.setBackground(new Color(248, 249, 250));
        demoPanel.setLayout(new BoxLayout(demoPanel, BoxLayout.Y_AXIS));
        demoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        demoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        demoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        
        JLabel demoTitle = new JLabel("🔑 Demo Login Credentials");
        demoTitle.setFont(new Font("Arial", Font.BOLD, 13));
        demoTitle.setForeground(new Color(63, 81, 181));
        demoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        String[] users = {
            "john / admin123 (Admin)",
            "jane / parent123 (Parent)",
            "tom / child123 (Child)"
        };
        
        demoPanel.add(demoTitle);
        demoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        for (String user : users) {
            JLabel userLabel2 = new JLabel(user);
            userLabel2.setFont(new Font("Courier New", Font.PLAIN, 12));
            userLabel2.setForeground(new Color(80, 80, 80));
            userLabel2.setAlignmentX(Component.LEFT_ALIGNMENT);
            demoPanel.add(userLabel2);
            demoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        formPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        formPanel.add(demoPanel);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        add(mainPanel);
        setVisible(true);
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter username and password!", 
                "Login Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Check credentials
        String fullName = null;
        String role = null;
        
        if (username.equals("john") && password.equals("admin123")) {
            fullName = "John Doe";
            role = "Admin";
        } else if (username.equals("jane") && password.equals("parent123")) {
            fullName = "Jane Smith";
            role = "Parent";
        } else if (username.equals("tom") && password.equals("child123")) {
            fullName = "Tom Wilson";
            role = "Child";
        } else {
            JOptionPane.showMessageDialog(this, 
                "Invalid username or password!", 
                "Login Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Success - open gallery
        this.dispose();
        new SimpleGallery(fullName);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimpleLogin());
    }
}
