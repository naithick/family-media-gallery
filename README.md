# Family Media Gallery

A secure desktop application for managing and sharing family photos with privacy controls.

## Team Members
- **Naithick** - Project Lead & Developer

## Features

- **User Management** - Secure login system with three user roles (Admin, Parent, Child)
- **Photo Upload** - Upload images with descriptions, tags, and album organization
- **Album Management** - Create and organize photos into custom albums
- **Sharing System** - Share albums with other family members using secure tokens
- **Privacy Controls** - Each user has their own private photo gallery
- **Search** - Find photos by description or tags
- **Family Members** - View all family members in the system

## Requirements

- Java 8 or higher
- Windows, macOS, or Linux

## Installation

1. Ensure Java is installed on your system:
   ```
   java -version
   ```

2. Clone or download this repository

3. Navigate to the project directory

## Running the Application

### Windows
Double-click `run.bat` or run in PowerShell:
```powershell
.\run.bat
```

### Manual Run
```bash
javac -encoding UTF-8 -source 8 -target 8 -d target/classes src/main/java/com/familymedia/imagegallery/*.java
java -cp target/classes com.familymedia.imagegallery.SimpleLogin
```

## Login Credentials

| Username | Password   | Role   |
|----------|-----------|--------|
| john     | admin123  | Admin  |
| jane     | parent123 | Parent |
| tom      | child123  | Child  |

## How to Use

### Upload Photos
1. Log in with your credentials
2. Click "Upload Photo" button
3. Select an image file
4. Add description, tags, and album
5. Click Upload

### Create Albums
1. Click "New Album" button
2. Enter album name and description
3. Photos can be organized into albums during upload

### Share Albums
1. Click "Share Album" button
2. Select the album to share
3. Set permissions (View Only, View & Download, or Full Access)
4. Choose expiration time (optional)
5. Set password protection (optional)
6. Copy the generated share token
7. Share the token with family members

### Access Shared Albums
1. Click "Access Shared" button
2. Enter the share token you received
3. Enter password if required
4. View the shared photos
5. Click "Back to My Photos" to return to your gallery

## Project Structure

```
pbl/
├── src/main/java/com/familymedia/imagegallery/
│   ├── SimpleLogin.java       # Login interface
│   └── SimpleGallery.java     # Main gallery application
├── gallery_data/
│   ├── images/                # Uploaded image files
│   ├── data.txt              # Image metadata
│   ├── albums.txt            # Album information
│   └── shares.txt            # Share tokens and permissions
├── run.bat                    # Windows startup script
└── README.md                  # This file
```

## Privacy & Security

- Each user has a completely private photo gallery
- Users can only view their own photos unless accessing shared albums
- Users can only delete their own photos
- Share tokens can expire and be password-protected
- All data is stored locally on your machine

## Troubleshooting

**Application won't start:**
- Verify Java 8+ is installed
- Check that all .java files are in the correct directory
- Try running the manual compile command

**Images not displaying:**
- Ensure images are in supported formats (JPG, PNG, GIF)
- Check that gallery_data/images/ folder exists
- Verify file permissions

**Login fails:**
- Use credentials exactly as shown in the table above
- Usernames and passwords are case-sensitive

## License

This project is created for educational purposes.

---

**Note:** This application stores all data locally in the `gallery_data` folder. Keep regular backups of this folder to preserve your photos and data.
