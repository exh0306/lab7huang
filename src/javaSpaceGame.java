/**
 * Project: Lab7
 * Purpose Details: Modifying SpaceGame
 * Course : IST 242
 * Author: Emlety Huang
 * Date Developed: 4/28/26
 * Last Date Changed: 4/30/26
 * Revision: 4/30/26
 *
 */

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.event.KeyEvent;


public class javaSpaceGame extends JFrame implements KeyListener {
    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private static final int PLAYER_WIDTH = 80;
    private static final int PLAYER_HEIGHT = 80;
    private static final int OBSTACLE_WIDTH = 15;
    private static final int OBSTACLE_HEIGHT = 10;
    private static final int PROJECTILE_WIDTH = 15;
    private static final int PROJECTILE_HEIGHT = 10;
    private static final int PLAYER_SPEED = 15;
    private static final int OBSTACLE_SPEED = 2;
    private static final int PROJECTILE_SPEED = 5;
    private int score = 0;

    private JPanel gamePanel;
    private JLabel scoreLabel;
    private Timer timer;
    private boolean isGameOver;
    private int playerX, playerY;
    private int projectileX, projectileY;
    private boolean isProjectileVisible;
    private boolean isFiring;

    //modifying
    private List<Point> obstacles;
    private BufferedImage shipImage;
    private List<Point> stars;
    private BufferedImage spriteSheet;
    private int spriteWidth = 100;
    private int spriteHeight = 64;
    private Clip clip;
    private boolean shieldActive = false;
    private int shieldDuration = 5000; // 5 seconds
    private long shieldStartTime;

    private void activateShield() {
        shieldActive = true;
        shieldStartTime = System.currentTimeMillis();
    }

    private boolean isShieldActive() {
        return shieldActive && (System.currentTimeMillis() - shieldStartTime) < shieldDuration;
    }


    public javaSpaceGame() {
        setTitle("Space Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        try {
            //Spaceship image
            shipImage = ImageIO.read(new File("ship.png"));

            //asteriods image
            spriteSheet = ImageIO.read(new File("ast.png"));

            //Audio wave sound
            AudioInputStream audioInputStream =
                    AudioSystem.getAudioInputStream(new File("fire_converted.wav"));

            clip = AudioSystem.getClip();
            clip.open(audioInputStream);

        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
            ex.printStackTrace();
        }


        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };

        //game score text shows blue
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setBounds(10, 10, 100, 20);
        scoreLabel.setForeground(Color.BLUE);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 40));
        gamePanel.add(scoreLabel);


        add(gamePanel);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);
        gamePanel.requestFocusInWindow();

        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 20;
        projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
        projectileY = playerY;
        isProjectileVisible = true;
        isGameOver = false;
        isFiring = false;
        obstacles = new java.util.ArrayList<>();

        stars = generateStars(200);

        timer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isGameOver) {
                    update();
                    gamePanel.repaint();
                }
            }
        });
        timer.start();
    }


    private void draw(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);


        //Draw stars with random color
        //g.setColor(generateRandomColor());
        Random rand = new Random();

        for (Point star : stars) {
            int brightness = 150 + rand.nextInt(106); // 150–255
            g.setColor(new Color(brightness, brightness, brightness));
            g.fillOval(star.x, star.y, 2, 2);
        }

        //Player = ship
        g.drawImage(shipImage, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, null);
        //g.setColor(Color.BLUE);
        //g.fillRect(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);

        //Projectile (what the spaceship shoots)
        if (isProjectileVisible) {
            g.setColor(Color.GREEN);
            g.fillRect(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }

        //Obstacles
        //g.setColor(Color.RED);
        //for (Point obstacle : obstacles) {
        //    g.fillRect(obstacle.x, obstacle.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
        //}

        for (Point obstacle : obstacles) {
            if (spriteSheet != null) {
                //Randomly select a sprite index (0-3)
                Random random = new Random();
                int spriteIndex = random.nextInt(4);
                // Calc the x y coord of the selected sprite on the sprite sheet
                int spriteX = spriteIndex * spriteWidth;
                int spriteY = 0; // Assuming all sprites are in the first row
                // Draw the selected sprite onto the canvas
                g.drawImage(spriteSheet.getSubimage(spriteX, spriteY,
                        spriteWidth, spriteHeight), obstacle.x, obstacle.y, null);
            }
        }

        if (isGameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Over!", WIDTH / 2 - 80, HEIGHT / 2);
        }
        if (isShieldActive()) {
            g.setColor(new Color(0, 255, 255, 100)); //Semi-transparent cyan
            g.fillOval(playerX, playerY, 80, 80);
        }
    }


    private List<Point> generateStars(int numStars) {
        List<Point> starsList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < numStars; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            starsList.add(new Point(x, y));
        }
        return starsList;
    }


    private void update() {
        if (!isGameOver) {
            // Move obstacles
            for (int i = 0; i < obstacles.size(); i++) {
                obstacles.get(i).y += OBSTACLE_SPEED;
                if (obstacles.get(i).y > HEIGHT) {
                    obstacles.remove(i);
                    i--;
                }
            }

            // Generate new obstacles
            if (Math.random() < 0.02) {
                int obstacleX = (int) (Math.random() * (WIDTH - OBSTACLE_WIDTH));
                obstacles.add(new Point(obstacleX, 0));
            }

            // Regenerate stars (twinkle / change background)
            if (Math.random() < 0.02) {
                stars = generateStars(200);
            }

            // Move projectile
            if (isProjectileVisible) {
                projectileY -= PROJECTILE_SPEED;
                if (projectileY < 0) {
                    isProjectileVisible = false;
                }
            }

            // Check collision with player
            Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
            for (Point obstacle : obstacles) {
                Rectangle obstacleRect = new Rectangle(obstacle.x, obstacle.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                if (playerRect.intersects(obstacleRect) && !isShieldActive()) {
                    isGameOver = true;
                    break;
                }
            }

            if (shieldActive) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - shieldStartTime > shieldDuration) {
                    shieldActive = false;
                }
            }


            // Check collision with obstacle
            Rectangle projectileRect = new Rectangle(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
            for (int i = 0; i < obstacles.size(); i++) {
                Rectangle obstacleRect = new Rectangle(obstacles.get(i).x, obstacles.get(i).y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                if (projectileRect.intersects(obstacleRect)) {
                    obstacles.remove(i);
                    score += 10;
                    isProjectileVisible = false;
                    break;
                }
            }

            scoreLabel.setText("Score: " + score);
        }
    }


    // Reset the game
    private void reset() {
        score = 0;
        isGameOver = false;
        repaint();
    }


    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        // Movement (independent)
        if (keyCode == KeyEvent.VK_LEFT && playerX > 0) {
            playerX -= PLAYER_SPEED;
        }

        if (keyCode == KeyEvent.VK_RIGHT && playerX < WIDTH - PLAYER_WIDTH) {
            playerX += PLAYER_SPEED;
        }

        // Shooting (ONLY when space is pressed)
        if (keyCode == KeyEvent.VK_SPACE && !isFiring) {
            isFiring = true;

            // play sound
            if (clip != null) {
                clip.setFramePosition(0);
                clip.start();
            }

            // shoot projectile
            projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
            projectileY = playerY;
            isProjectileVisible = true;

            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    isFiring = false;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        }

        // Shield
        if (keyCode == KeyEvent.VK_CONTROL) {
            activateShield();
        }
        //reset the game
        if (keyCode == KeyEvent.VK_ESCAPE) {
            reset();
        }
    }




    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new javaSpaceGame().setVisible(true);
            }
        });
    }
}
