/**
 * Project: Lab7
 * Purpose Details: Modifying SpaceGame
 * Course : IST 242
 * Author: Emlety Huang
 * Date Developed: 4/28/26
 * Last Date Changed: 5/3/26
 * Revision: 5/3/26
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
    private int playerHealth = 10;
    private int gameTime = 60; //secs
    private long lastTimeUpdate = System.currentTimeMillis();
    private int level = 1;
    private int levelScoreTarget = 50; //the score you need to move to next level
    private int obstacleSpeed = 2;
    private double spawnRate = 0.02;

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
                // Calc the x y cord of the selected sprite on the sprite sheet
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

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("Health: " + playerHealth, 10, 60); //shows player's health


        g.setColor(Color.GREEN);
        for (Point hp : healthPowerUps) {
            g.fillOval(hp.x, hp.y, 20, 20);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("Time: " + gameTime, WIDTH - 150, 60); //countdown timer

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Level: " + level, WIDTH - 30 - 100, 100);

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

    private List<Point> healthPowerUps = new ArrayList<>();

    private void update() {
        if (!isGameOver) {
            // Move obstacles
            for (int i = 0; i < obstacles.size(); i++) {
                obstacles.get(i).y += obstacleSpeed;
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

            for (int i = 0; i < obstacles.size(); i++) {
                Point obstacle = obstacles.get(i);

                Rectangle obstacleRect = new Rectangle(obstacle.x, obstacle.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);

                if (playerRect.intersects(obstacleRect)) {

                    if (!shieldActive) {
                        playerHealth--;

                        obstacles.remove(i);
                        i--;

                        if (playerHealth <= 0) {
                            isGameOver = true;
                        }
                    }
                    break;
                }
            }

            if (shieldActive) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - shieldStartTime > shieldDuration) {
                    shieldActive = false;
                }
            }

            if (Math.random() < 0.01) {  //Spawn rate
                int x = (int) (Math.random() * (WIDTH - 20));
                healthPowerUps.add(new Point(x, 0));
            }
            for (int i = 0; i < healthPowerUps.size(); i++) {
                healthPowerUps.get(i).y += OBSTACLE_SPEED;

                if (healthPowerUps.get(i).y > HEIGHT) {
                    healthPowerUps.remove(i);
                    i--; // adjust index after removal
                }

                System.out.println("PowerUps: " + healthPowerUps.size());

                long currentTime = System.currentTimeMillis();
                if (currentTime - lastTimeUpdate >= 1000) { // 1 sec passed
                    gameTime--;
                    lastTimeUpdate = currentTime;
                    if (gameTime <= 0) {
                    isGameOver = true;}
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

            // LEVEL UP SYSTEM
            if (score >= levelScoreTarget) {
                levelUp();
            }
        }
    }


    // Reset the game
    private void reset() {
        score = 0;
        isGameOver = false;
        repaint();

        gameTime = 60;
        lastTimeUpdate = System.currentTimeMillis();
        playerHealth = 10;
    }

    private void levelUp() {
        level++;

        // increase difficulty
        levelScoreTarget += 100;
        obstacleSpeed += 1;
        spawnRate += 0.01;

        // optional: reward player
        playerHealth = Math.min(playerHealth + 2, 10);

        System.out.println("Level Up! Now Level " + level);
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
