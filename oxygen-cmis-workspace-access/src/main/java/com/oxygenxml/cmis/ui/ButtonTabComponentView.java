package com.oxygenxml.cmis.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * A view that constructs the tab button and takes care of it's behavior
 * 
 * @author bluecc
 *
 */
public class ButtonTabComponentView extends JPanel {
  private final JTabbedPane pane;

  /**
   * Constructs the button using the pane
   * 
   * @exception NullPointerException
   * @param pane
   */
  public ButtonTabComponentView(final JTabbedPane pane) {

    // unset default FlowLayout' gaps
    super(new FlowLayout(FlowLayout.LEFT, 0, 0));

    if (pane == null) {
      throw new NullPointerException("TabbedPane is null");
    }
    this.pane = pane;

    setOpaque(false);

    // make JLabel read titles from JTabbedPane
    final JLabel label = new JLabel() {
      @Override
      public String getText() {

        // Get the index
        final int i = pane.indexOfTabComponent(ButtonTabComponentView.this);
        if (i != -1) {
          return pane.getTitleAt(i);
        }
        return null;
      }
    };

    add(label);
    // Add more space between the label and the button
    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

    // Tab button
    final JButton button = new TabButton();
    add(button);

    // Add more space to the top of the component
    setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
  }

  /**
   * The custom button and it's painting
   * 
   * @author bluecc
   *
   */
  private class TabButton extends JButton implements ActionListener {
    public TabButton() {
      final int size = 17;

      setPreferredSize(new Dimension(size, size));
      setToolTipText("close this tab");

      // Make the button looks the same for all Laf's
      setUI(new BasicButtonUI());

      // Make it transparent
      setContentAreaFilled(false);

      // No need to be focusable
      setFocusable(false);
      setBorder(BorderFactory.createEtchedBorder());
      setBorderPainted(false);

      // Making nice rollover effect
      // we use the same listener for all buttons
      addMouseListener(buttonMouseListener);
      setRolloverEnabled(true);

      // Close the proper tab by clicking the button
      addActionListener(this);
    }

    /**
     * Remove the button on remove and decrease the number of the items in tabs
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      final int i = pane.indexOfTabComponent(ButtonTabComponentView.this);
      if (i != -1) {
        pane.remove(i);
        TabComponentsView.itemsCounter--;
      }
    }

    // We don't want to update UI for this button
    @Override
    public void updateUI() {
    }

    // Paint the cross
    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      final Graphics2D g2 = (Graphics2D) g.create();

      // Shift the image for pressed buttons
      if (getModel().isPressed()) {
        g2.translate(1, 1);
      }
      g2.setStroke(new BasicStroke(2));
      g2.setColor(Color.BLACK);
      if (getModel().isRollover()) {
        g2.setColor(Color.MAGENTA);
      }
      final int delta = 6;
      g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
      g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
      g2.dispose();
    }

  }

  // Set the button border when mouse is inside
  private static final MouseAdapter buttonMouseListener = new MouseAdapter() {

    @Override
    public void mouseEntered(MouseEvent e) {

      final Component component = e.getComponent();

      if (component instanceof AbstractButton) {

        final AbstractButton button = (AbstractButton) component;
        button.setBorderPainted(true);
      }
    }

    // Remove the button border when mouse is outside
    @Override
    public void mouseExited(MouseEvent e) {

      final Component component = e.getComponent();

      if (component instanceof AbstractButton) {

        final AbstractButton button = (AbstractButton) component;

        button.setBorderPainted(false);
      }
    }
  };
}
