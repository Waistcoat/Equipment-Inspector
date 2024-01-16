package equipmentinspector;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemComposition;
import net.runelite.api.kit.KitType;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import net.runelite.api.Client;

@Slf4j
@Singleton
public class EquipmentInspectorPanel extends PluginPanel
{
    private final static String NO_PLAYER_SELECTED = "No player selected";

    private GridBagConstraints c;
    private JPanel equipmentPanels;
    private JPanel header;
    public JLabel nameLabel;

    @Inject
    private Client client;
    @Setter
    private BufferedImage membersImage;
    @Inject
    private ItemManager itemManager;

    public EquipmentInspectorPanel()
    {

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        equipmentPanels = new JPanel(new GridBagLayout());
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;

        header = new JPanel();
        header.setLayout(new BorderLayout());
        header.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(58, 58, 58)),
                BorderFactory.createEmptyBorder(0, 0, 10, 0)));

        nameLabel = new JLabel(NO_PLAYER_SELECTED);
        nameLabel.setForeground(Color.WHITE);

        header.add(nameLabel, BorderLayout.CENTER);

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(equipmentPanels)
                .addComponent(header)
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(header)
                .addGap(10)
                .addComponent(equipmentPanels)
        );

        update(new HashMap<>(), new HashMap<>(), "");
    }

    public void update(Map<KitType, ItemComposition> playerEquipment, Map<KitType, Integer> equipmentPrices, String playerName)
    {

        if (playerName.isEmpty() || playerName == null)
        {
            nameLabel.setText(NO_PLAYER_SELECTED);
        }
        else
        {
            nameLabel.setText("Player: " + playerName);
        }

        SwingUtilities.invokeLater(() ->
                {
                    equipmentPanels.removeAll();
                    AtomicLong totalItemPrice= new AtomicLong();
                    playerEquipment.forEach((kitType, itemComposition) ->
                    {
                        AsyncBufferedImage itemImage = itemManager.getImage(itemComposition.getId());
                        int itemPrice = equipmentPrices.get(kitType);
                        totalItemPrice.addAndGet(itemPrice);
                        equipmentPanels.add(new ItemPanel(itemComposition, kitType, itemImage, itemPrice, membersImage), c);
                        c.gridy++;

                    });
                    if(!nameLabel.getText().equals(NO_PLAYER_SELECTED)) {
                        equipmentPanels.add(new TotalPanel(totalItemPrice.get()), c);
                        c.gridy++;
                    }
                    header.revalidate();
                    header.repaint();
                }
        );
    }
}