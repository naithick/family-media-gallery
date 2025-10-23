package com.familymedia.imagegallery;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SimpleGallery extends JFrame {
    private String currentUser;
    private JPanel galleryPanel;
    private JLabel statusLabel;
    private JComboBox<String> albumSelector;
    private List<ImageInfo> images = new ArrayList<>();
    private List<AlbumInfo> albums = new ArrayList<>();
    private List<ShareInfo> shares = new ArrayList<>();
    private String viewingSharedFrom = null; // Track if viewing shared album
    private static final String IMAGES_DIR = "gallery_data/images";
    private static final String DATA_FILE = "gallery_data/data.txt";
    private static final String ALBUMS_FILE = "gallery_data/albums.txt";
    private static final String SHARES_FILE = "gallery_data/shares.txt";
    
    static class ImageInfo {
        String fileName;
        String description;
        String uploader;
        String uploadDate;
        String album;
        String tags;
        
        ImageInfo(String fileName, String description, String uploader, String uploadDate, String album, String tags) {
            this.fileName = fileName;
            this.description = description;
            this.uploader = uploader;
            this.uploadDate = uploadDate;
            this.album = album;
            this.tags = tags;
        }
    }
    
    static class AlbumInfo {
        String name;
        String description;
        String owner;
        
        AlbumInfo(String name, String description, String owner) {
            this.name = name;
            this.description = description;
            this.owner = owner;
        }
    }
    
    static class ShareInfo {
        String token;
        String albumName;
        String sharedBy;
        String createdAt;
        String expiresAt;
        String permission;
        String password;
        
        ShareInfo(String token, String albumName, String sharedBy, String createdAt, 
                  String expiresAt, String permission, String password) {
            this.token = token;
            this.albumName = albumName;
            this.sharedBy = sharedBy;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.permission = permission;
            this.password = password;
        }
    }
    
    public SimpleGallery(String userName) {
        this.currentUser = userName;
        initStorage();
        loadAlbums();
        loadImages();
        loadShares();
        setupUI();
    }
    
    private void initStorage() {
        try {
            Files.createDirectories(Paths.get(IMAGES_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void setupUI() {
        setTitle("Family Gallery - " + currentUser);
        setSize(1400, 900);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Main Layout with Sidebar
        setLayout(new BorderLayout());
        
        // Top Bar - Simplified
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(63, 81, 181));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        
        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftTop.setOpaque(false);
        
        JLabel titleLabel = new JLabel("� Family Gallery");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        leftTop.add(titleLabel);
        
        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightTop.setOpaque(false);
        
        // Album selector in top bar
        JLabel albumLabelTop = new JLabel("Album:");
        albumLabelTop.setFont(new Font("Arial", Font.BOLD, 13));
        albumLabelTop.setForeground(Color.WHITE);
        
        albumSelector = new JComboBox<>();
        albumSelector.setFont(new Font("Arial", Font.PLAIN, 13));
        albumSelector.setBackground(Color.WHITE);
        albumSelector.setPreferredSize(new Dimension(180, 35));
        albumSelector.addItem("All Images");
        for (AlbumInfo album : albums) {
            if (album.owner.equals(currentUser)) {
                albumSelector.addItem(album.name);
            }
        }
        albumSelector.addActionListener(e -> refreshGallery());
        
        JLabel userLabel = new JLabel("� " + currentUser);
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        userLabel.setForeground(Color.WHITE);
        userLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        
        rightTop.add(albumLabelTop);
        rightTop.add(albumSelector);
        rightTop.add(userLabel);
        
        topPanel.add(leftTop, BorderLayout.WEST);
        topPanel.add(rightTop, BorderLayout.EAST);
        
        // LEFT SIDEBAR for actions
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(250, 250, 250));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));
        sidebar.setPreferredSize(new Dimension(220, 900));
        
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Section: Photos
        JLabel photoSection = new JLabel("  PHOTOS");
        photoSection.setFont(new Font("Arial", Font.BOLD, 11));
        photoSection.setForeground(new Color(120, 120, 120));
        photoSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(photoSection);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        
        sidebar.add(createSidebarButton("📤 Upload Photo", new Color(76, 175, 80), e -> uploadPhoto()));
        sidebar.add(createSidebarButton("🔍 Search Photos", new Color(121, 85, 72), e -> searchPhotos()));
        
        sidebar.add(Box.createRigidArea(new Dimension(0, 25)));
        
        // Section: Albums
        JLabel albumSection = new JLabel("  ALBUMS");
        albumSection.setFont(new Font("Arial", Font.BOLD, 11));
        albumSection.setForeground(new Color(120, 120, 120));
        albumSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(albumSection);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        
        sidebar.add(createSidebarButton("📁 New Album", new Color(255, 152, 0), e -> createAlbum()));
        
        sidebar.add(Box.createRigidArea(new Dimension(0, 25)));
        
        // Section: Sharing
        JLabel shareSection = new JLabel("  SHARING");
        shareSection.setFont(new Font("Arial", Font.BOLD, 11));
        shareSection.setForeground(new Color(120, 120, 120));
        shareSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(shareSection);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        
        sidebar.add(createSidebarButton("🔗 Share Album", new Color(156, 39, 176), e -> shareAlbum()));
        sidebar.add(createSidebarButton("🔓 Access Shared", new Color(0, 150, 136), e -> accessShared()));
        sidebar.add(createSidebarButton("📊 My Shares", new Color(233, 30, 99), e -> viewMyShares()));
        sidebar.add(createSidebarButton("◀ Back to My Photos", new Color(255, 152, 0), e -> {
            viewingSharedFrom = null;
            albumSelector.setSelectedIndex(0);
            refreshGallery();
            statusLabel.setText("Returned to your photos");
        }));
        
        sidebar.add(Box.createRigidArea(new Dimension(0, 25)));
        
        // Section: Family
        JLabel familySection = new JLabel("  FAMILY");
        familySection.setFont(new Font("Arial", Font.BOLD, 11));
        familySection.setForeground(new Color(120, 120, 120));
        familySection.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(familySection);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        
        sidebar.add(createSidebarButton("👨‍👩‍👧‍👦 Family Members", new Color(63, 81, 181), e -> manageFamilyMembers()));
        
        sidebar.add(Box.createVerticalGlue());
        
        // Refresh button at bottom
        sidebar.add(createSidebarButton("🔄 Refresh", new Color(33, 150, 243), e -> {
            loadAlbums();
            loadImages();
            loadShares();
            refreshGallery();
        }));
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Gallery Panel
        galleryPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        galleryPanel.setBackground(new Color(245, 245, 245));
        galleryPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JScrollPane scrollPane = new JScrollPane(galleryPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        
        // Status Bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(238, 238, 238));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        // Add all to frame
        add(topPanel, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        
        refreshGallery();
        setVisible(true);
    }
    
    private JButton createSidebarButton(String text, Color bgColor, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        
        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        
        return btn;
    }
    
    private void uploadPhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif"));
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            
            // Create upload dialog
            JDialog uploadDialog = new JDialog(this, "Upload Photo", true);
            uploadDialog.setLayout(new BorderLayout(10, 10));
            uploadDialog.setSize(450, 350);
            uploadDialog.setLocationRelativeTo(this);
            
            JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
            formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JLabel descLabel = new JLabel("Description:");
            JTextField descField = new JTextField();
            
            JLabel tagsLabel = new JLabel("Tags (comma separated):");
            JTextField tagsField = new JTextField();
            
            JLabel albumLabel = new JLabel("Album:");
            JComboBox<String> albumCombo = new JComboBox<>();
            albumCombo.addItem("None");
            for (AlbumInfo album : albums) {
                if (album.owner.equals(currentUser)) {
                    albumCombo.addItem(album.name);
                }
            }
            
            JLabel fileLabel = new JLabel("File:");
            JLabel fileNameLabel = new JLabel(selectedFile.getName());
            
            formPanel.add(descLabel);
            formPanel.add(descField);
            formPanel.add(tagsLabel);
            formPanel.add(tagsField);
            formPanel.add(albumLabel);
            formPanel.add(albumCombo);
            formPanel.add(fileLabel);
            formPanel.add(fileNameLabel);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            JButton uploadButton = new JButton("Upload");
            uploadButton.setBackground(new Color(76, 175, 80));
            uploadButton.setForeground(Color.WHITE);
            uploadButton.setFocusPainted(false);
            uploadButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
            uploadButton.addActionListener(e -> {
                String description = descField.getText().trim();
                if (description.isEmpty()) description = "No description";
                
                String tags = tagsField.getText().trim();
                String album = albumCombo.getSelectedItem().toString();
                
                try {
                    String newFileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                    Path destPath = Paths.get(IMAGES_DIR, newFileName);
                    Files.copy(selectedFile.toPath(), destPath);
                    
                    String uploadDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    ImageInfo info = new ImageInfo(newFileName, description, currentUser, uploadDate, album, tags);
                    images.add(info);
                    saveImages();
                    
                    refreshGallery();
                    statusLabel.setText("Photo uploaded successfully!");
                    uploadDialog.dispose();
                    
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(uploadDialog, 
                        "Failed to upload photo: " + ex.getMessage(), 
                        "Upload Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            });
            
            JButton cancelButton = new JButton("Cancel");
            cancelButton.setBackground(new Color(158, 158, 158));
            cancelButton.setForeground(Color.WHITE);
            cancelButton.setFocusPainted(false);
            cancelButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
            cancelButton.addActionListener(e -> uploadDialog.dispose());
            
            buttonPanel.add(uploadButton);
            buttonPanel.add(cancelButton);
            
            uploadDialog.add(formPanel, BorderLayout.CENTER);
            uploadDialog.add(buttonPanel, BorderLayout.SOUTH);
            uploadDialog.setVisible(true);
        }
    }
    
    private void refreshGallery() {
        galleryPanel.removeAll();
        
        String selectedAlbum = albumSelector != null ? albumSelector.getSelectedItem().toString() : "All Images";
        List<ImageInfo> filteredImages = new ArrayList<>();
        
        // Determine whose photos to show
        String photoOwner = viewingSharedFrom != null ? viewingSharedFrom : currentUser;
        
        // Filter images by owner and album
        for (ImageInfo img : images) {
            if (img.uploader.equals(photoOwner)) {
                if (selectedAlbum.equals("All Images") || selectedAlbum.equals(img.album)) {
                    filteredImages.add(img);
                }
            }
        }
        
        if (filteredImages.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setBackground(new Color(245, 245, 245));
            
            JPanel messagePanel = new JPanel();
            messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
            messagePanel.setBackground(Color.WHITE);
            messagePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(40, 40, 40, 40)
            ));
            
            JLabel iconLabel = new JLabel("📷");
            iconLabel.setFont(new Font("Arial", Font.PLAIN, 60));
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel emptyLabel = new JLabel(viewingSharedFrom != null ? 
                "No photos in this shared album" : "No photos yet");
            emptyLabel.setFont(new Font("Arial", Font.BOLD, 18));
            emptyLabel.setForeground(new Color(100, 100, 100));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel hintLabel = new JLabel(viewingSharedFrom != null ? 
                "The album owner hasn't uploaded any photos here yet" : 
                "Click 'Upload Photo' to get started!");
            hintLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            hintLabel.setForeground(new Color(150, 150, 150));
            hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            messagePanel.add(iconLabel);
            messagePanel.add(Box.createRigidArea(new Dimension(0, 15)));
            messagePanel.add(emptyLabel);
            messagePanel.add(Box.createRigidArea(new Dimension(0, 8)));
            messagePanel.add(hintLabel);
            
            emptyPanel.add(messagePanel);
            galleryPanel.setLayout(new BorderLayout());
            galleryPanel.add(emptyPanel, BorderLayout.CENTER);
        } else {
            galleryPanel.setLayout(new GridLayout(0, 3, 15, 15));
            for (ImageInfo info : filteredImages) {
                galleryPanel.add(createPhotoCard(info));
            }
        }
        
        galleryPanel.revalidate();
        galleryPanel.repaint();
        
        String viewingText = viewingSharedFrom != null ? 
            " (Viewing " + viewingSharedFrom + "'s photos)" : "";
        statusLabel.setText(filteredImages.size() + " photo(s) in " + selectedAlbum + viewingText);
    }
    
    private JPanel createPhotoCard(ImageInfo info) {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        
        // Add hover effect
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(63, 81, 181), 2),
                    BorderFactory.createEmptyBorder(0, 0, 0, 0)
                ));
            }
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                    BorderFactory.createEmptyBorder(0, 0, 0, 0)
                ));
            }
        });
        
        try {
            BufferedImage img = ImageIO.read(new File(IMAGES_DIR, info.fileName));
            Image scaled = img.getScaledInstance(350, 280, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaled));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setBackground(new Color(248, 248, 248));
            imageLabel.setOpaque(true);
            imageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            imageLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    showFullImage(info);
                }
            });
            
            // Info Panel with gradient-like effect
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setBackground(Color.WHITE);
            infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            
            JLabel descLabel = new JLabel(truncate(info.description, 40));
            descLabel.setFont(new Font("Arial", Font.BOLD, 14));
            descLabel.setForeground(new Color(30, 30, 30));
            descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            // Meta info with icons
            JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            metaPanel.setBackground(Color.WHITE);
            metaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JLabel uploaderLabel = new JLabel("👤 " + info.uploader);
            uploaderLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            uploaderLabel.setForeground(new Color(100, 100, 100));
            
            JLabel dateLabel = new JLabel("  📅 " + info.uploadDate);
            dateLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            dateLabel.setForeground(new Color(100, 100, 100));
            
            metaPanel.add(uploaderLabel);
            metaPanel.add(dateLabel);
            
            // Album & Tags info
            if (!info.album.equals("None") || !info.tags.isEmpty()) {
                JPanel extraPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                extraPanel.setBackground(Color.WHITE);
                extraPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                if (!info.album.equals("None")) {
                    JLabel albumLabel = new JLabel("📁 " + info.album);
                    albumLabel.setFont(new Font("Arial", Font.PLAIN, 11));
                    albumLabel.setForeground(new Color(100, 100, 100));
                    extraPanel.add(albumLabel);
                }
                
                if (!info.tags.isEmpty()) {
                    JLabel tagsLabel = new JLabel("  🏷️ " + truncate(info.tags, 20));
                    tagsLabel.setFont(new Font("Arial", Font.PLAIN, 11));
                    tagsLabel.setForeground(new Color(100, 100, 100));
                    extraPanel.add(tagsLabel);
                }
                
                infoPanel.add(extraPanel);
                infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
            
            // Buttons with better styling
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            buttonPanel.setBackground(Color.WHITE);
            buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JButton viewBtn = createCardButton("👁️ View", new Color(63, 81, 181));
            viewBtn.addActionListener(e -> showFullImage(info));
            
            JButton downloadBtn = createCardButton("⬇️ Download", new Color(76, 175, 80));
            downloadBtn.addActionListener(e -> downloadPhoto(info));
            
            JButton deleteBtn = createCardButton("🗑️ Delete", new Color(244, 67, 54));
            deleteBtn.addActionListener(e -> deletePhoto(info));
            
            buttonPanel.add(viewBtn);
            buttonPanel.add(downloadBtn);
            buttonPanel.add(deleteBtn);
            
            infoPanel.add(descLabel);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            infoPanel.add(metaPanel);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            infoPanel.add(buttonPanel);
            
            card.add(imageLabel, BorderLayout.CENTER);
            card.add(infoPanel, BorderLayout.SOUTH);
            
        } catch (IOException e) {
            JLabel errorLabel = new JLabel("❌ Error loading image");
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            errorLabel.setForeground(new Color(244, 67, 54));
            errorLabel.setFont(new Font("Arial", Font.BOLD, 14));
            card.add(errorLabel, BorderLayout.CENTER);
        }
        
        return card;
    }
    
    private JButton createCardButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        
        return btn;
    }
    
    private void showFullImage(ImageInfo info) {
        JDialog dialog = new JDialog(this, "Photo Viewer", true);
        dialog.setLayout(new BorderLayout());
        
        try {
            BufferedImage img = ImageIO.read(new File(IMAGES_DIR, info.fileName));
            
            // Scale to fit screen
            int maxWidth = 900;
            int maxHeight = 700;
            double scale = Math.min((double)maxWidth / img.getWidth(), (double)maxHeight / img.getHeight());
            int scaledWidth = (int)(img.getWidth() * scale);
            int scaledHeight = (int)(img.getHeight() * scale);
            
            Image scaled = img.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaled));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            JPanel infoPanel = new JPanel();
            infoPanel.setBackground(Color.WHITE);
            infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            
            JLabel descLabel = new JLabel(info.description);
            descLabel.setFont(new Font("Arial", Font.BOLD, 14));
            JLabel uploaderLabel = new JLabel("Uploaded by: " + info.uploader);
            uploaderLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            JLabel dateLabel = new JLabel("Date: " + info.uploadDate);
            dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            
            infoPanel.add(descLabel);
            infoPanel.add(uploaderLabel);
            infoPanel.add(dateLabel);
            
            dialog.add(new JScrollPane(imageLabel), BorderLayout.CENTER);
            dialog.add(infoPanel, BorderLayout.SOUTH);
            
            dialog.setSize(1000, 800);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading image", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void downloadPhoto(ImageInfo info) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(info.fileName.substring(37))); // Remove UUID
        
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Files.copy(
                    Paths.get(IMAGES_DIR, info.fileName),
                    chooser.getSelectedFile().toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                );
                statusLabel.setText("Photo downloaded successfully!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Download failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deletePhoto(ImageInfo info) {
        // Only allow deletion if it's the current user's photo
        if (!info.uploader.equals(currentUser)) {
            JOptionPane.showMessageDialog(this, 
                "You can only delete your own photos!", 
                "Permission Denied", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this photo?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Files.deleteIfExists(Paths.get(IMAGES_DIR, info.fileName));
                images.remove(info);
                saveImages();
                refreshGallery();
                statusLabel.setText("Photo deleted successfully!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Delete failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void loadImages() {
        images.clear();
        File dataFile = new File(DATA_FILE);
        if (dataFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 4) {
                        String album = parts.length > 4 ? parts[4] : "None";
                        String tags = parts.length > 5 ? parts[5] : "";
                        images.add(new ImageInfo(parts[0], parts[1], parts[2], parts[3], album, tags));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void saveImages() {
        try {
            Files.createDirectories(Paths.get("gallery_data"));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
                for (ImageInfo info : images) {
                    writer.write(info.fileName + "|" + info.description + "|" + info.uploader + "|" + 
                                info.uploadDate + "|" + info.album + "|" + info.tags);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadAlbums() {
        albums.clear();
        File albumFile = new File(ALBUMS_FILE);
        if (albumFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(albumFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length == 3) {
                        albums.add(new AlbumInfo(parts[0], parts[1], parts[2]));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void saveAlbums() {
        try {
            Files.createDirectories(Paths.get("gallery_data"));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(ALBUMS_FILE))) {
                for (AlbumInfo album : albums) {
                    writer.write(album.name + "|" + album.description + "|" + album.owner);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadShares() {
        shares.clear();
        File shareFile = new File(SHARES_FILE);
        if (shareFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(shareFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length == 7) {
                        shares.add(new ShareInfo(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void saveShares() {
        try {
            Files.createDirectories(Paths.get("gallery_data"));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(SHARES_FILE))) {
                for (ShareInfo share : shares) {
                    writer.write(share.token + "|" + share.albumName + "|" + share.sharedBy + "|" + 
                                share.createdAt + "|" + share.expiresAt + "|" + share.permission + "|" + share.password);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void createAlbum() {
        JDialog dialog = new JDialog(this, "Create New Album", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel nameLabel = new JLabel("Album Name:");
        JTextField nameField = new JTextField();
        
        JLabel descLabel = new JLabel("Description:");
        JTextField descField = new JTextField();
        
        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(descLabel);
        formPanel.add(descField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton createButton = new JButton("Create");
        createButton.setBackground(new Color(255, 152, 0));
        createButton.setForeground(Color.WHITE);
        createButton.setFocusPainted(false);
        createButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        createButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Album name is required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if album exists
            for (AlbumInfo album : albums) {
                if (album.name.equals(name)) {
                    JOptionPane.showMessageDialog(dialog, "Album already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            albums.add(new AlbumInfo(name, desc, currentUser));
            saveAlbums();
            
            // Update album selector
            albumSelector.addItem(name);
            
            statusLabel.setText("Album '" + name + "' created!");
            dialog.dispose();
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(158, 158, 158));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void shareAlbum() {
        if (albums.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No albums to share! Create an album first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JDialog dialog = new JDialog(this, "Share Album", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel albumLabel = new JLabel("Select Album:");
        JComboBox<String> albumCombo = new JComboBox<>();
        for (AlbumInfo album : albums) {
            albumCombo.addItem(album.name);
        }
        
        JLabel permLabel = new JLabel("Permission:");
        JComboBox<String> permCombo = new JComboBox<>();
        permCombo.addItem("View Only");
        permCombo.addItem("View & Download");
        permCombo.addItem("View, Download & Upload");
        
        JLabel expiresLabel = new JLabel("Expires:");
        JComboBox<String> expiresCombo = new JComboBox<>();
        expiresCombo.addItem("Never");
        expiresCombo.addItem("1 Hour");
        expiresCombo.addItem("1 Day");
        expiresCombo.addItem("7 Days");
        expiresCombo.addItem("30 Days");
        
        JLabel passLabel = new JLabel("Password (optional):");
        JPasswordField passField = new JPasswordField();
        
        JLabel tokenLabel = new JLabel("Share Token:");
        JTextField tokenField = new JTextField(UUID.randomUUID().toString().substring(0, 10).toUpperCase());
        tokenField.setEditable(false);
        
        formPanel.add(albumLabel);
        formPanel.add(albumCombo);
        formPanel.add(permLabel);
        formPanel.add(permCombo);
        formPanel.add(expiresLabel);
        formPanel.add(expiresCombo);
        formPanel.add(passLabel);
        formPanel.add(passField);
        formPanel.add(tokenLabel);
        formPanel.add(tokenField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton createButton = new JButton("Create Share Link");
        createButton.setBackground(new Color(156, 39, 176));
        createButton.setForeground(Color.WHITE);
        createButton.setFocusPainted(false);
        createButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        createButton.addActionListener(e -> {
            String albumName = albumCombo.getSelectedItem().toString();
            String permission = permCombo.getSelectedItem().toString();
            String expiresOption = expiresCombo.getSelectedItem().toString();
            String password = new String(passField.getPassword());
            String token = tokenField.getText();
            
            String createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            String expiresAt = calculateExpiry(expiresOption);
            
            ShareInfo share = new ShareInfo(token, albumName, currentUser, createdAt, expiresAt, permission, password);
            shares.add(share);
            saveShares();
            
            // Show success dialog with copy button
            JDialog successDialog = new JDialog(dialog, "Share Link Created!", true);
            successDialog.setLayout(new BorderLayout(10, 10));
            successDialog.setSize(450, 300);
            successDialog.setLocationRelativeTo(dialog);
            
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            infoPanel.setBackground(Color.WHITE);
            
            JLabel successLabel = new JLabel("✅ Share Link Created Successfully!");
            successLabel.setFont(new Font("Arial", Font.BOLD, 16));
            successLabel.setForeground(new Color(76, 175, 80));
            successLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel albumLabel2 = new JLabel("Album: " + albumName);
            albumLabel2.setFont(new Font("Arial", Font.PLAIN, 14));
            albumLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel permLabel2 = new JLabel("Permission: " + permission);
            permLabel2.setFont(new Font("Arial", Font.PLAIN, 14));
            permLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel expiresLabel2 = new JLabel("Expires: " + expiresAt);
            expiresLabel2.setFont(new Font("Arial", Font.PLAIN, 14));
            expiresLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JPanel tokenPanel = new JPanel(new BorderLayout(10, 10));
            tokenPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(63, 81, 181), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
            tokenPanel.setBackground(new Color(232, 234, 246));
            tokenPanel.setMaximumSize(new Dimension(400, 80));
            
            JLabel tokenTitleLabel = new JLabel("Share Token:");
            tokenTitleLabel.setFont(new Font("Arial", Font.BOLD, 12));
            
            JTextField tokenDisplayField = new JTextField(token);
            tokenDisplayField.setFont(new Font("Courier New", Font.BOLD, 18));
            tokenDisplayField.setEditable(false);
            tokenDisplayField.setHorizontalAlignment(JTextField.CENTER);
            tokenDisplayField.setBackground(Color.WHITE);
            
            JButton copyBtn = new JButton("📋 Copy Token");
            copyBtn.setFont(new Font("Arial", Font.BOLD, 13));
            copyBtn.setBackground(new Color(63, 81, 181));
            copyBtn.setForeground(Color.WHITE);
            copyBtn.setFocusPainted(false);
            copyBtn.setBorderPainted(false);
            copyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            copyBtn.addActionListener(ev -> {
                java.awt.datatransfer.StringSelection stringSelection = 
                    new java.awt.datatransfer.StringSelection(token);
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(stringSelection, null);
                copyBtn.setText("✅ Copied!");
                copyBtn.setBackground(new Color(76, 175, 80));
            });
            
            tokenPanel.add(tokenTitleLabel, BorderLayout.NORTH);
            tokenPanel.add(tokenDisplayField, BorderLayout.CENTER);
            tokenPanel.add(copyBtn, BorderLayout.SOUTH);
            
            infoPanel.add(successLabel);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            infoPanel.add(albumLabel2);
            infoPanel.add(permLabel2);
            infoPanel.add(expiresLabel2);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            infoPanel.add(tokenPanel);
            
            JButton closeBtn = new JButton("Close");
            closeBtn.setFont(new Font("Arial", Font.BOLD, 14));
            closeBtn.setBackground(new Color(158, 158, 158));
            closeBtn.setForeground(Color.WHITE);
            closeBtn.setFocusPainted(false);
            closeBtn.setBorderPainted(false);
            closeBtn.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
            closeBtn.addActionListener(ev -> successDialog.dispose());
            
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            btnPanel.add(closeBtn);
            
            successDialog.add(infoPanel, BorderLayout.CENTER);
            successDialog.add(btnPanel, BorderLayout.SOUTH);
            successDialog.setVisible(true);
            
            statusLabel.setText("Share link created for album: " + albumName);
            dialog.dispose();
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(158, 158, 158));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void accessShared() {
        JDialog dialog = new JDialog(this, "Access Shared Album", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel tokenLabel = new JLabel("Share Token:");
        JTextField tokenField = new JTextField();
        
        JLabel passLabel = new JLabel("Password (if required):");
        JPasswordField passField = new JPasswordField();
        
        formPanel.add(tokenLabel);
        formPanel.add(tokenField);
        formPanel.add(passLabel);
        formPanel.add(passField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton accessButton = new JButton("Access");
        accessButton.setBackground(new Color(0, 150, 136));
        accessButton.setForeground(Color.WHITE);
        accessButton.setFocusPainted(false);
        accessButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        accessButton.addActionListener(e -> {
            String token = tokenField.getText().trim();
            String password = new String(passField.getPassword());
            
            if (token.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a token!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Find share
            ShareInfo foundShare = null;
            for (ShareInfo share : shares) {
                if (share.token.equals(token)) {
                    foundShare = share;
                    break;
                }
            }
            
            if (foundShare == null) {
                JOptionPane.showMessageDialog(dialog, "Invalid token!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check expiry
            if (!foundShare.expiresAt.equals("Never")) {
                LocalDateTime expiry = LocalDateTime.parse(foundShare.expiresAt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                if (LocalDateTime.now().isAfter(expiry)) {
                    JOptionPane.showMessageDialog(dialog, "This share link has expired!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // Check password
            if (!foundShare.password.isEmpty() && !foundShare.password.equals(password)) {
                JOptionPane.showMessageDialog(dialog, "Incorrect password!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Access granted - Set the shared owner to view their photos
            viewingSharedFrom = foundShare.sharedBy;
            
            // Show success message with option to return
            int result = JOptionPane.showConfirmDialog(dialog, 
                "Access Granted!\n\nAlbum: " + foundShare.albumName + 
                "\nPermission: " + foundShare.permission + 
                "\nShared by: " + foundShare.sharedBy + 
                "\n\nYou are now viewing " + foundShare.sharedBy + "'s photos." +
                "\n\nClick OK to continue, or Cancel to return to your photos.", 
                "Success", 
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
            
            if (result == JOptionPane.CANCEL_OPTION) {
                viewingSharedFrom = null;
                dialog.dispose();
                return;
            }
            
            // Set album selector to the shared album
            albumSelector.setSelectedItem(foundShare.albumName);
            refreshGallery();
            
            statusLabel.setText("Viewing shared album: " + foundShare.albumName + " (by " + foundShare.sharedBy + ")");
            dialog.dispose();
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(158, 158, 158));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(accessButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private String calculateExpiry(String option) {
        if (option.equals("Never")) return "Never";
        
        LocalDateTime expiry = LocalDateTime.now();
        switch (option) {
            case "1 Hour": expiry = expiry.plusHours(1); break;
            case "1 Day": expiry = expiry.plusDays(1); break;
            case "7 Days": expiry = expiry.plusDays(7); break;
            case "30 Days": expiry = expiry.plusDays(30); break;
        }
        
        return expiry.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    private void viewMyShares() {
        List<ShareInfo> myShares = new ArrayList<>();
        for (ShareInfo share : shares) {
            if (share.sharedBy.equals(currentUser)) {
                myShares.add(share);
            }
        }
        
        if (myShares.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You haven't created any share links yet!", "No Shares", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JDialog dialog = new JDialog(this, "My Share Links", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(this);
        
        String[] columns = {"Token", "Album", "Permission", "Created", "Expires", "Status"};
        Object[][] data = new Object[myShares.size()][6];
        
        for (int i = 0; i < myShares.size(); i++) {
            ShareInfo share = myShares.get(i);
            String status = "Active";
            
            if (!share.expiresAt.equals("Never")) {
                try {
                    LocalDateTime expiry = LocalDateTime.parse(share.expiresAt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    if (LocalDateTime.now().isAfter(expiry)) {
                        status = "Expired";
                    }
                } catch (Exception e) {
                    status = "Unknown";
                }
            }
            
            data[i][0] = share.token;
            data[i][1] = share.albumName;
            data[i][2] = share.permission;
            data[i][3] = share.createdAt;
            data[i][4] = share.expiresAt;
            data[i][5] = status;
        }
        
        JTable table = new JTable(data, columns);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(63, 81, 181));
        table.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton copyBtn = new JButton("📋 Copy Selected Token");
        copyBtn.setFont(new Font("Arial", Font.BOLD, 13));
        copyBtn.setBackground(new Color(63, 81, 181));
        copyBtn.setForeground(Color.WHITE);
        copyBtn.setFocusPainted(false);
        copyBtn.setBorderPainted(false);
        copyBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        copyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        copyBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String token = (String) table.getValueAt(row, 0);
                java.awt.datatransfer.StringSelection stringSelection = 
                    new java.awt.datatransfer.StringSelection(token);
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(stringSelection, null);
                statusLabel.setText("Token copied to clipboard!");
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a row first!", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        JButton deleteBtn = new JButton("🗑️ Delete Selected");
        deleteBtn.setFont(new Font("Arial", Font.BOLD, 13));
        deleteBtn.setBackground(new Color(244, 67, 54));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String token = (String) table.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(dialog, 
                    "Delete this share link?", 
                    "Confirm", 
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    shares.removeIf(s -> s.token.equals(token));
                    saveShares();
                    dialog.dispose();
                    viewMyShares();
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a row first!", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 13));
        closeBtn.setBackground(new Color(158, 158, 158));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        closeBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(copyBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(closeBtn);
        
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void searchPhotos() {
        String searchTerm = JOptionPane.showInputDialog(this, 
            "Enter search term (description or tags):", 
            "Search Photos", 
            JOptionPane.PLAIN_MESSAGE);
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return;
        }
        
        searchTerm = searchTerm.toLowerCase();
        List<ImageInfo> results = new ArrayList<>();
        
        // Only search current user's photos
        for (ImageInfo img : images) {
            if (img.uploader.equals(currentUser)) {
                if (img.description.toLowerCase().contains(searchTerm) || 
                    img.tags.toLowerCase().contains(searchTerm)) {
                    results.add(img);
                }
            }
        }
        
        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No photos found matching: " + searchTerm, 
                "Search Results", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Show results in a dialog
        JDialog dialog = new JDialog(this, "Search Results: " + results.size() + " photo(s)", false);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(1000, 700);
        dialog.setLocationRelativeTo(this);
        
        JPanel resultsPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        resultsPanel.setBackground(new Color(245, 245, 245));
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        for (ImageInfo info : results) {
            resultsPanel.add(createPhotoCard(info));
        }
        
        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    private void manageFamilyMembers() {
        JDialog dialog = new JDialog(this, "👨‍👩‍👧‍👦 Family Members", true);
        dialog.setLayout(new BorderLayout(15, 15));
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Family Members");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Available users
        String[][] familyMembers = {
            {"John Doe", "Admin", "john"},
            {"Jane Smith", "Parent", "jane"},
            {"Tom Wilson", "Child", "tom"}
        };
        
        for (String[] member : familyMembers) {
            JPanel memberPanel = new JPanel(new BorderLayout(15, 0));
            memberPanel.setBackground(new Color(248, 249, 250));
            memberPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
            memberPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            memberPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            // Left: Avatar and info
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);
            
            JLabel nameLabel = new JLabel("👤 " + member[0]);
            nameLabel.setFont(new Font("Arial", Font.BOLD, 15));
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JLabel roleLabel = new JLabel(member[1] + " • @" + member[2]);
            roleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            roleLabel.setForeground(new Color(100, 100, 100));
            roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            infoPanel.add(nameLabel);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            infoPanel.add(roleLabel);
            
            // Right: Status
            JLabel statusLabel2 = new JLabel(member[0].equals(currentUser) ? "✓ You" : "Active");
            statusLabel2.setFont(new Font("Arial", Font.BOLD, 12));
            statusLabel2.setForeground(member[0].equals(currentUser) ? 
                new Color(76, 175, 80) : new Color(100, 100, 100));
            
            memberPanel.add(infoPanel, BorderLayout.CENTER);
            memberPanel.add(statusLabel2, BorderLayout.EAST);
            
            contentPanel.add(memberPanel);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        
        contentPanel.add(Box.createVerticalGlue());
        
        // Info box
        JPanel infoBox = new JPanel();
        infoBox.setLayout(new BoxLayout(infoBox, BoxLayout.Y_AXIS));
        infoBox.setBackground(new Color(232, 245, 233));
        infoBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(76, 175, 80), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        infoBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        JLabel infoTitle = new JLabel("💡 Tip");
        infoTitle.setFont(new Font("Arial", Font.BOLD, 13));
        infoTitle.setForeground(new Color(46, 125, 50));
        infoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel infoText = new JLabel("<html>Share albums with family members using the Share feature.<br>Each member has their own private photo collection!</html>");
        infoText.setFont(new Font("Arial", Font.PLAIN, 12));
        infoText.setForeground(new Color(27, 94, 32));
        infoText.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoBox.add(infoTitle);
        infoBox.add(Box.createRigidArea(new Dimension(0, 5)));
        infoBox.add(infoText);
        
        contentPanel.add(infoBox);
        
        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        closeBtn.setBackground(new Color(63, 81, 181));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(closeBtn);
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private String truncate(String text, int length) {
        if (text.length() <= length) return text;
        return text.substring(0, length - 3) + "...";
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimpleGallery("John Doe"));
    }
}
