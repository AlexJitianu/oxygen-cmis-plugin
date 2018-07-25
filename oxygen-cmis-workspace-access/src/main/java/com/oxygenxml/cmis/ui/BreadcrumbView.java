package com.oxygenxml.cmis.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.oxygenxml.cmis.core.model.IResource;

import ro.sync.exml.workspace.api.standalone.ui.ToolbarButton;

public class BreadcrumbView extends JPanel implements BreadcrumbPresenter {
  private JToolBar toolBar;
  private JPanel breadcrumbPanel;
  private JLabel goUpIcon;
  private ItemsPresenter itemsPresenter;

  
  /*
   * The stack that takes care of the order
   */
  private Stack<IResource> parentResources;
  private Stack<JButton> hiddenItems;

  BreadcrumbView(ItemsPresenter itemsPresenter) {
    // Initialize data
    this.itemsPresenter = itemsPresenter;
    parentResources = new Stack<IResource>();
    hiddenItems = new Stack<JButton>();

    toolBar = new JToolBar();
    breadcrumbPanel = new JPanel();
    goUpIcon = new JLabel();

    // Design the toolbar
    toolBar.setFloatable(false);
    toolBar.setRollover(true);

    // Add listener to the toolbar
    toolBar.addComponentListener(new ComponentAdapter() {

      public void componentResized(ComponentEvent e) {
        doBreadcrumbsLayout();
      }

    });

    // Set the icon
    goUpIcon.setIcon(UIManager.getIcon("FileChooser.upFolderIcon"));

    // Set layout
    setLayout(new BorderLayout());
    breadcrumbPanel.setLayout(new GridBagLayout());

    // Set constraints
    GridBagConstraints c = new GridBagConstraints();

    // GoUpIcon
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.0;

    breadcrumbPanel.add(goUpIcon, c);

    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;

    breadcrumbPanel.add(toolBar, c);
    add(breadcrumbPanel, BorderLayout.CENTER);
  };

  
  /*
   * Custom JButton for JToolbar
   */
  public JButton customJButton(final IResource resource) {

    // Action is the listener of of button event
    Action action = new AbstractAction() {

      @Override
      public void actionPerformed(ActionEvent e) {

        // While goes back to the target selected pop elements
        // Remove descendants from the visible toolbar.
        while (!resource.getId().equals(parentResources.peek().getId()) && toolBar.getComponentCount() > 0) {

          System.out.println("Eliminate: " + parentResources.peek().getDisplayName());
          toolBar.remove(toolBar.getComponentCount() - 1);
          parentResources.pop();
        }

        // If toolBar does not have items
        if (toolBar.getComponentCount() == 0) {

          // Remove items from stacks till there are hiddenItems
          while (hiddenItems.peek() != null) {

            IResource pop = parentResources.peek();

            // Check if reached the target by ID
            if (resource.getId().equals(pop.getId())) {

              hiddenItems.pop();

              // Break when found
              break;
            } else {

              // Pop until we reach the target
              parentResources.pop();
              hiddenItems.pop();
            }

          }
        }

        // Present the resources (children) of the items
        if (!parentResources.isEmpty()) {
          itemsPresenter.presentFolderItems(parentResources.peek().getId());
        }

        // Revalidate toolBar view and refresh
        toolBar.revalidate();
        toolBar.repaint();
      }
    };

    // Create a button using Oxygen button class
    action.putValue(Action.NAME, resource.getDisplayName());

    // Attach the event created
    ToolbarButton currentButton = new ToolbarButton(action, true);

    System.out.println("Button pref size: " + currentButton.getPreferredSize());

    return currentButton;
  };

  
  /*
   * popUp JButton for JToolbar
   * 
   * @return JButton
   */
  public JButton popUpJButton(final String resource) {

    // Create the event of the button
    Action action = new AbstractAction() {

      @Override
      public void actionPerformed(ActionEvent e) {

        // Create the menu of the popUp button
        JPopupMenu popUpMenu = new JPopupMenu();

        // Iterate over all elements and attach the action of each
        for (JButton jButton : hiddenItems) {

          JMenuItem menuItem = new JMenuItem(jButton.getAction());

          // Add always the popUpButton
          popUpMenu.add(menuItem, 0);
        }

        // Show the popUpMenu
        Component source = (Component) e.getSource();
        popUpMenu.show(source, 0, source.getHeight());

      }
    };
    // Put the value of the button
    action.putValue(Action.NAME, resource);
    // Add event to the button
    ToolbarButton popUpButton = new ToolbarButton(action, true);

    return popUpButton;
  };

  
  /*
   * Present the breadcrumb
   */
  @Override
  public void presentBreadcrumb(IResource resource) {

    // Push to the parents stack
    parentResources.push(resource);
    System.out.println("Go to breadcrumb=" + parentResources.peek().getDisplayName());

    // Add to the toolBar the popUp
    // ----------------------------
    JButton customJButton = customJButton(resource);
    System.out.println("Preferred size toolbar=" + toolBar.getPreferredSize());

    toolBar.add(customJButton);

    // Revalidate to not show an empty component
    getParent().revalidate();
    getParent().repaint();

    // Invoke later to make sure the component is added
    // and has a dimension
    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {

        // Check the if has necessary width
        boolean hasEnoughWidth = hasEnoughWidth(customJButton);
        if (!hasEnoughWidth) {

          System.out.println("\nDoes not have enough space!");
          // Refresh breadcrumb layout
          doBreadcrumbsLayout();

        }
      }
    });
    // ----------------------------

    System.out.println("Actual size button: " + customJButton.getSize());
  };

  
  /*
   * Check of can fit the toolBar
   * 
   * @return boolean
   */
  private boolean hasEnoughWidth(JButton customJButton) {
    boolean hasWidth = (getComponentsWidth(toolBar) + customJButton.getPreferredSize().getWidth()) < toolBar.getWidth();
    return hasWidth;
  };

  
  /*
   * Reset the whole breadcrumb and data from it
   */
  @Override
  public void resetBreadcrumb(boolean flag) {
    if (flag) {

      // Remove old data
      parentResources.removeAllElements();
      hiddenItems.removeAllElements();
      toolBar.removeAll();

      // Revalidate to not show an empty component
      getParent().revalidate();
      getParent().repaint();
    }

  };

  /*
   * @return integer width of components from JToolBar
   */
  int getComponentsWidth(JToolBar toolbar) {
    int totalWidth = 0;
    Component[] components = toolBar.getComponents();
    for (int i = 0; i < components.length; i++) {
      totalWidth += components[i].getWidth();
    }
    return totalWidth;
  };

  /*
   * Makes sure that items are either pushed to the hiddenItems stack or pushed
   * back to the breadcrumb if they fit the toolBar at that moment
   */
  private void doBreadcrumbsLayout() {
    //Print the children
    System.out.println("\nToolbar size: " + toolBar.getSize());
    Component[] components = toolBar.getComponents();
    for (int i = 0; i < components.length; i++) {
      System.out.println("Child " + i + "=" + components[i].getSize());

    }

    // Check if there is a widget
    boolean existsMoreWidget = false;
    if (toolBar.getComponentCount() > 0) {
      
      JButton first = (JButton) toolBar.getComponent(0);
      
      if (first.getText().equals("..")) {
        existsMoreWidget = true;
      }
    }

    // Push to the hiddenItems stack if the components do not fit the toolBar 
    // and remove from toolBar
    int counter = existsMoreWidget ? 1 : 0;
    while (getComponentsWidth(toolBar) > toolBar.getWidth()) {

      hiddenItems.push((JButton) toolBar.getComponentAtIndex(counter));
      toolBar.remove(counter);

    }

    //  Push to the toolBar and remove from hiddenItems stack till they fit the toolBar
    int componentsWidth = getComponentsWidth(toolBar);
    while (componentsWidth < toolBar.getWidth() && !hiddenItems.isEmpty()) {
      
      JButton item = hiddenItems.peek();
      double childWidth = item.getPreferredSize().getWidth();
      
      // Add the child width
      componentsWidth += childWidth;
      
      if (componentsWidth < toolBar.getWidth()) {
        toolBar.add(item, counter);

        hiddenItems.pop();
      }
    }
    System.out.println("Empty =" + hiddenItems.isEmpty());
    System.out.println("no widget=" + existsMoreWidget);

    // Add the "more items" widget if needed.
    if (!hiddenItems.isEmpty() && !existsMoreWidget) {

      // This will break the UI
      System.out.println("Add popup");
      toolBar.add(popUpJButton(".."), 0);
    } else if (hiddenItems.isEmpty() && existsMoreWidget) {
      toolBar.remove(0);
    }

    // Revalidate to not show an empty component and refresh
    getParent().revalidate();
    getParent().repaint();
  };

};
