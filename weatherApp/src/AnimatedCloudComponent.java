package weatherApp.src;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class AnimatedCloudComponent extends JPanel {
    private float animationOffset = 0;
    private Timer animationTimer;
    private String weatherCondition = "Clear";

    public AnimatedCloudComponent() {
        setOpaque(false);
        setPreferredSize(new Dimension(450, 217));

        // Animation timer - updates every 50ms for smooth animation
        animationTimer = new Timer(50, e -> {
            animationOffset += 0.5f;
            if (animationOffset > 360) {
                animationOffset = 0;
            }
            repaint();
        });
        animationTimer.start();
    }

    public void setWeatherCondition(String condition) {
        this.weatherCondition = condition;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        // Enable anti-aliasing for smooth graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;

        switch (weatherCondition) {
            case "Cloudy":
                drawAnimatedClouds(g2d, centerX, centerY);
                break;
            case "Clear":
                drawAnimatedSun(g2d, centerX, centerY);
                break;
            case "Rain":
                drawAnimatedRain(g2d, centerX, centerY);
                break;
            case "Snow":
                drawAnimatedSnow(g2d, centerX, centerY);
                break;
            default:
                drawAnimatedClouds(g2d, centerX, centerY);
        }

        g2d.dispose();
    }

    private void drawAnimatedClouds(Graphics2D g2d, int centerX, int centerY) {
        // Create floating effect with sine wave
        float floatOffset = (float) Math.sin(Math.toRadians(animationOffset * 2)) * 5;

        // Main cloud - light gray with gradient
        GradientPaint cloudGradient = new GradientPaint(
                centerX - 60, centerY - 30, new Color(220, 220, 220, 200),
                centerX + 60, centerY + 30, new Color(180, 180, 180, 200));
        g2d.setPaint(cloudGradient);

        // Cloud made of overlapping circles
        drawCloud(g2d, centerX, centerY + (int) floatOffset, 1.0f);

        // Secondary cloud - slightly transparent
        g2d.setPaint(new Color(200, 200, 200, 150));
        float secondaryOffset = (float) Math.sin(Math.toRadians(animationOffset * 1.5)) * 3;
        drawCloud(g2d, centerX - 80, centerY - 40 + (int) secondaryOffset, 0.7f);

        // Tertiary cloud
        g2d.setPaint(new Color(190, 190, 190, 120));
        float tertiaryOffset = (float) Math.sin(Math.toRadians(animationOffset * 2.5)) * 4;
        drawCloud(g2d, centerX + 70, centerY - 20 + (int) tertiaryOffset, 0.6f);
    }

    private void drawCloud(Graphics2D g2d, int x, int y, float scale) {
        int size = (int) (40 * scale);

        // Cloud made of overlapping circles
        g2d.fill(new Ellipse2D.Float(x - size, y - size / 2, size * 1.5f, size));
        g2d.fill(new Ellipse2D.Float(x - size / 2, y - size, size * 1.2f, size));
        g2d.fill(new Ellipse2D.Float(x, y - size / 2, size * 1.3f, size));
        g2d.fill(new Ellipse2D.Float(x + size / 3, y - size / 3, size, size * 0.8f));
        g2d.fill(new Ellipse2D.Float(x - size / 3, y, size * 1.1f, size * 0.7f));
    }

    private void drawAnimatedSun(Graphics2D g2d, int centerX, int centerY) {
        // Rotating sun rays
        g2d.setColor(new Color(255, 215, 0));
        g2d.setStroke(new BasicStroke(3));

        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30 + animationOffset);
            int rayLength = 60;
            int x1 = centerX + (int) (Math.cos(angle) * 45);
            int y1 = centerY + (int) (Math.sin(angle) * 45);
            int x2 = centerX + (int) (Math.cos(angle) * (45 + rayLength));
            int y2 = centerY + (int) (Math.sin(angle) * (45 + rayLength));
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Sun circle with gradient
        RadialGradientPaint sunGradient = new RadialGradientPaint(
                centerX, centerY, 40,
                new float[] { 0f, 1f },
                new Color[] { new Color(255, 255, 100), new Color(255, 215, 0) });
        g2d.setPaint(sunGradient);
        g2d.fill(new Ellipse2D.Float(centerX - 40, centerY - 40, 80, 80));
    }

    private void drawAnimatedRain(Graphics2D g2d, int centerX, int centerY) {
        // Draw cloud first
        drawAnimatedClouds(g2d, centerX, centerY);

        // Animated raindrops
        g2d.setColor(new Color(100, 150, 255, 180));
        g2d.setStroke(new BasicStroke(2));

        for (int i = 0; i < 15; i++) {
            int x = centerX - 100 + (i * 15);
            int dropOffset = (int) ((animationOffset * 3 + i * 20) % 100);
            int y1 = centerY + 40 + dropOffset;
            int y2 = y1 + 20;
            g2d.drawLine(x, y1, x, y2);
        }
    }

    private void drawAnimatedSnow(Graphics2D g2d, int centerX, int centerY) {
        // Draw cloud first
        drawAnimatedClouds(g2d, centerX, centerY);

        // Animated snowflakes
        g2d.setColor(new Color(255, 255, 255, 200));

        for (int i = 0; i < 20; i++) {
            int x = centerX - 120 + (i * 12);
            float snowOffset = (animationOffset + i * 15) % 150;
            int y = centerY + 30 + (int) snowOffset;

            // Simple snowflake shape
            g2d.fill(new Ellipse2D.Float(x - 3, y - 3, 6, 6));
            g2d.drawLine(x - 5, y, x + 5, y);
            g2d.drawLine(x, y - 5, x, y + 5);
        }
    }

    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

    public void startAnimation() {
        if (animationTimer != null && !animationTimer.isRunning()) {
            animationTimer.start();
        }
    }
}