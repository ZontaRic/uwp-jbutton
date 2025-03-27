package javaswingdev.uwp;

// Original: uwp-jbutton https://github.com/DJ-Raven/uwp-jbutton (DJ-Raven)
// Changed 03.2025: Ric Zonta
// Problem with the Library TimingFramework-0.55.jar
// When starting a process with processbuilder and waiting for the termination of the forked process (.waitfor()), the timers go berserk and create a high CPU-procentage
// Therefore we use javax.swing.Timer to eliminate the problem

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.*;

public class UWPButton extends JButton {

    private float animateOver = 0;
    private float animatePress = 0;
    private boolean mouseOver = false;
    private boolean mousePress = false;
    private int borderSize = 2;
    private Point mousePoint;
    private Color selectedColor = new Color(200, 200, 200);
    private Color effectColor = new Color(255, 255, 255);
    private Timer overTimer;
    private Timer pressTimer;

    public UWPButton() {
        init();
    }

    private void init() {
        setContentAreaFilled(false);
        setBackground(new Color(52, 153, 252));
        setForeground(Color.WHITE);
        initTimers();
        
        MouseAdapter mouseEvent = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseOver = true;
                startAnimationOver();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseOver = false;
                startAnimationOver();
            }

            @Override
            public void mousePressed(MouseEvent me) {
                if (SwingUtilities.isLeftMouseButton(me)) {
                    mousePress = true;
                    startAnimationPress();
                }
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                if (SwingUtilities.isLeftMouseButton(me)) {
                    mousePress = false;
                    startAnimationPress();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mousePoint = e.getPoint();
                repaint();
            }
        };
        addMouseListener(mouseEvent);
        addMouseMotionListener(mouseEvent);
    }

    private void initTimers() {
        overTimer = createAnimationTimer(() -> animateOver, f -> animateOver = f);
        pressTimer = createAnimationTimer(() -> animatePress, f -> animatePress = f);
    }

    private Timer createAnimationTimer(Supplier<Float> getter, Consumer<Float> setter) {
        return new Timer(15, e -> {
            float fraction = getter.get();
            if (mouseOver || mousePress) {
                fraction = Math.min(fraction + 0.05f, 1f);
            } else {
                fraction = Math.max(fraction - 0.05f, 0f);
            }
            setter.accept(fraction);
            repaint();
            if (fraction == 0 || fraction == 1) {
                ((Timer) e.getSource()).stop();
            }
        });
    }

    private void startAnimationOver() {
        overTimer.start();
    }

    private void startAnimationPress() {
        pressTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int x = 0;
        int y = 0;
        int width = getWidth();
        int height = getHeight();
        Rectangle rec = new Rectangle(x, y, width, height);

        if (isEnabled()) {
            g2.setColor(getBackground());
            g2.fill(rec);
            
            if (animateOver > 0 || animatePress > 0) {
                Area area = new Area(rec);
                Rectangle rec_in = new Rectangle(x + borderSize, y + borderSize, width - borderSize * 2, height - borderSize * 2);
                area.subtract(new Area(rec_in));
                
                if (animateOver > 0 && mousePoint != null) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, animateOver));
                    g2.setPaint(getGradient(mousePoint, 255, 1.5f));
                    g2.fill(area);
                    g2.setPaint(getGradient(mousePoint, 70, 0.3f));
                    g2.fill(rec_in);
                }
                
                if (animatePress > 0) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, animatePress));
                    g2.setColor(selectedColor);
                    g2.fill(rec_in);
                }
            }
        } else {
            g2.setColor(Color.GRAY);
            g2.fill(rec);
        }
        g2.dispose();
        super.paintComponent(g);
    }

    private RadialGradientPaint getGradient(Point point, int alpha, float size) {
        int width = getWidth();
        int height = getHeight();
        Point2D center = point;
        float radius = (float) Math.max(width, height) * size;
        float[] dist = {0.0f, 1.0f};
        int red = effectColor.getRed();
        int green = effectColor.getGreen();
        int blue = effectColor.getBlue();
        Color[] colors = {new Color(red, green, blue, alpha), new Color(red, green, blue, 0)};
        return new RadialGradientPaint(center, radius, dist, colors);
    }

    public int getBorderSize() {
        return borderSize;
    }

    public void setBorderSize(int borderSize) {
        this.borderSize = borderSize;
        repaint();
    }

    public Color getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(Color selectedColor) {
        this.selectedColor = selectedColor;
        repaint();
    }

    public Color getEffectColor() {
        return effectColor;
    }

    public void setEffectColor(Color effectColor) {
        this.effectColor = effectColor;
        repaint();
    }
}
