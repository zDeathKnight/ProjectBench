package bau5.mods.projectbench.client;

import invtweaks.api.ContainerGUI;
import invtweaks.api.ContainerSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import bau5.mods.projectbench.common.ContainerProjectBench;
import bau5.mods.projectbench.common.ContainerProjectBenchII;
import bau5.mods.projectbench.common.ProjectBench;
import bau5.mods.projectbench.common.TEProjectBenchII;
import bau5.mods.projectbench.common.TileEntityProjectBench;
import bau5.mods.projectbench.common.packets.PBPacketHandler;
import bau5.mods.projectbench.common.packets.PBPacketManager;
import cpw.mods.fml.common.network.PacketDispatcher;

/**
 * ProjectBenchGui
 *
 * @author _bau5
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 * 
 */

@ContainerGUI
public class ProjectBenchGui extends GuiContainer {
	private int ID;
	private String texPath;
	private int lastIndex = -1;
	private ItemStack[] stacksToDraw = null;
	private boolean once = true;
	private boolean changed = false;
	private boolean panel = false;

	private int matrixStartX;
	private int matrixStopX;
	private int matrixStartY;
	private int matrixStopY;
	
	private int xShift = 0;
	private int yShift = 0;
	
	public ProjectBenchGui(InventoryPlayer inventoryPlayer,
			TileEntity tileEntity, int guiID) {
		super((guiID == 0) ? new ContainerProjectBench(inventoryPlayer, (TileEntityProjectBench)tileEntity) 
							  : new ContainerProjectBenchII(inventoryPlayer, (TEProjectBenchII)tileEntity));
		ySize += 48;
		ID = guiID;
		if(ID == 0){
			texPath = ProjectBench.baseTexFile + "/gui/pbGUI.png";
		}
		else if (ID == 1){
			texPath = ProjectBench.baseTexFile + "/gui/pbGUI2.png";
			panel = true;
		}
	}
	
	@ContainerGUI.ContainerSectionCallback
	public Map<ContainerSection, List<Slot>> getContainerSections(){
		Map<ContainerSection, List<Slot>> result = new HashMap<ContainerSection, List<Slot>>();
		if(inventorySlots instanceof ContainerProjectBenchII){
			result.put(ContainerSection.CHEST, inventorySlots.inventorySlots.subList(27, 45));
		}
		else if(inventorySlots instanceof ContainerProjectBench){
			result.put(ContainerSection.CRAFTING_OUT, inventorySlots.inventorySlots.subList(0, 1));
			result.put(ContainerSection.CRAFTING_IN_PERSISTENT, inventorySlots.inventorySlots.subList(1, 10));
			result.put(ContainerSection.CHEST, inventorySlots.inventorySlots.subList(10, 28));
		}
		return result;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		if(ID == 0)
			fontRenderer.drawString("Project Bench", 8, 6, 4210752);
		else if(ID == 1)
			fontRenderer.drawString("Project Bench Mk. II", 8, 6, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2,
			int par3) {
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		if(once && ID == 0){
			once = false;
			buttonList.add(new PBClearInventoryButton(15294, width/2 -76, height/2 -49, 11, 11, ""));
		}else if(once && ID == 1){
			matrixStartX = guiLeft+7;
			matrixStopX  = matrixStartX+162;
			matrixStartY = guiTop +15;
			matrixStopY  = matrixStartY +58;
			xShift = -3;
			yShift = 2;
			buttonList.add(new PBScrambleMatrixButton(15294, x-8+xShift, y +19 +yShift, 9, 9, ""));
			buttonList.add( new PBRefreshMatrixButton(15295, x-8+xShift, y +31 +yShift, 9, 9, ""));
			once = false;
		}
		mc.renderEngine.bindTexture(texPath);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawTexturedModalRect(x+xShift, y+yShift, 0, 0, xSize, ySize);
		if(panel){
			this.drawTexturedModalRect(x+xShift-11, y+yShift+14, 185, 1, 12, 29);
		}
	}
	@Override
	protected void drawItemStackTooltip(ItemStack stack, int x,
			int y) {
		super.drawItemStackTooltip(stack, x, y);
		if(ID == 1){
			if(x >= matrixStartX && x < matrixStopX && y >= matrixStartY && y <= matrixStopY){
				drawRecipeToolTip((x-matrixStartX)/18, (y-matrixStartY)/18, x, y);
			}
		}
	}
	
	private void drawRecipeToolTip(int slotX, int slotY, int mouseX, int mouseY){
		int slotIndex = (slotX + (slotY * 9));
		if(slotIndex != lastIndex || changed){
			changed = false;
			lastIndex = slotIndex;
			stacksToDraw = ((ContainerProjectBenchII)inventorySlots).getStacksToConsumeForSlot(slotIndex);
		}
		if(stacksToDraw != null){
	        GL11.glTranslatef(0.0F, 0.0F, 32.0F);
	        this.zLevel = 200.0F;
	        float prevZ = itemRenderer.zLevel;
	        itemRenderer.zLevel = 300.0F;
	        drawGradientRect(mouseX+9, mouseY+1, mouseX + (16 * (stacksToDraw.length + 1)) - 7, 3+mouseY + 16, -99999999, -99999999);
	        for(int i = 0; i < stacksToDraw.length; i++){
	        	GL11.glPushMatrix();
		        FontRenderer font = stacksToDraw[i].getItem().getFontRenderer(stacksToDraw[i]);
		        if (font == null) font = fontRenderer;
		        itemRenderer.renderItemAndEffectIntoGUI(font, this.mc.renderEngine, stacksToDraw[i], 9 + mouseX + (i * 16), mouseY+2);
		        itemRenderer.renderItemOverlayIntoGUI(font, this.mc.renderEngine, stacksToDraw[i], 9 + mouseX + (i * 16), mouseY+2, Integer.toString(stacksToDraw[i].stackSize));
		        GL11.glPopMatrix();
	        }
	        this.zLevel = 0.0F;
	        itemRenderer.zLevel = prevZ;
		}
	}
	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);
		if(par3 == 1)
			changed = true;
	}
	@Override
	protected void handleMouseClick(Slot par1Slot, int par2, int par3, int par4) {
		if (par1Slot != null) {
            par2 = par1Slot.slotNumber;
        }
		if(ID == 1){
			if(par1Slot == null){
				super.handleMouseClick(par1Slot, par2, par3, par4);
				return;
			}else if(par1Slot.slotNumber >= 27){
			    this.mc.playerController.windowClick(this.inventorySlots.windowId, par2, par3, par4, this.mc.thePlayer);
				if(par1Slot != null && par1Slot.getHasStack() && ID == 1 && par1Slot.slotNumber >= 27 && par1Slot.slotNumber < 45)
					((ContainerProjectBenchII)inventorySlots).updateToolTipMap();
			}else if(par1Slot.slotNumber >= 0 && par1Slot.slotNumber < 27){
				craftingMatrixSlotClick(par2, par3, par4);
			}
		}else
			super.handleMouseClick(par1Slot, par2, par3, par4);
	}
	
	private void craftingMatrixSlotClick(int par1, int par2, int par3){
		short short1 = mc.thePlayer.openContainer.getNextTransactionID(mc.thePlayer.inventory);
		ItemStack stackInInv = ((ContainerProjectBenchII)mc.thePlayer.openContainer).getItemStackFromTileEntity(par1);
		if(stackInInv != null){
			int stackSize = stackInInv.stackSize;
	        ItemStack itemstack = mc.thePlayer.openContainer.slotClick(par1, par2, par3, mc.thePlayer);
	        PacketDispatcher.sendPacketToServer(PBPacketManager.getMkIIWindowClick(inventorySlots.windowId, par1, par2, par3, itemstack, short1, stackSize));
		}
	}
	
	public class PBClearInventoryButton extends GuiButton{

		public PBClearInventoryButton(int id, int xPos, int yPos, int width,
				int height, String label) {
			super(id, xPos, yPos, width, height, label);
		}
		@Override
		public void drawButton(Minecraft mc, int i, int j) {
			if (this.drawButton)
	        {
	            FontRenderer fontrenderer = mc.fontRenderer;
	            mc.renderEngine.bindTexture(texPath);
	            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	            this.field_82253_i = i >= this.xPosition && j >= this.yPosition && i < this.xPosition + this.width && j < this.yPosition + this.height;
	            int k = this.getHoverState(this.field_82253_i);
	            //Top left, top right, bottom left, bottom right, icon
	            this.drawTexturedModalRect(this.xPosition, this.yPosition, 176, 0 + (k-1)*11, width, height); 
	            this.mouseDragged(mc, i, j);
	            int l = 14737632;

	            if (!this.enabled)
	            {
	                l = -6250336;
	            }
	            else if (this.field_82253_i)
	            {
	                l = 16777120;
	            }

	            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, l);
	        }
		}
		@Override
		public boolean mousePressed(Minecraft mc, int par2, int par3) {
			boolean fireButton = super.mousePressed(mc, par2, par3);
			if(fireButton){
	            mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketDispatcher.sendPacketToServer(new Packet250CustomPayload(PBPacketHandler.PACKET_CHANNEL, new byte[]{1}));
			}
			return fireButton;
		}
	}
	
	public class PBScrambleMatrixButton extends GuiButton{
		private boolean clicked = false;
		private int displayTicks = 0;
		public PBScrambleMatrixButton(int id, int xPos, int yPos, int width,
				int height, String label) {
			super(id, xPos, yPos, width, height, label);
		}
		
		@Override
		public void drawButton(Minecraft par1Minecraft, int i, int j) {
			if(this.drawButton){
				if(clicked){
					if(++displayTicks > 10){
						clicked = false;
						displayTicks = 0;
					}
				}
				FontRenderer fontrenderer = mc.fontRenderer;
	            mc.renderEngine.bindTexture(texPath);
	            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	            this.field_82253_i = i >= this.xPosition && j >= this.yPosition && i < this.xPosition + this.width && j < this.yPosition + this.height;
	            int k = this.getHoverState(this.field_82253_i);
	            //Top left, top right, bottom left, bottom right, icon
	            GL11.glPushMatrix();
	            this.drawTexturedModalRect(this.xPosition, this.yPosition, 176, 3 + (k-1)*9*(clicked ? 2:1), width, height);
	            GL11.glPopMatrix();
	            this.mouseDragged(mc, i, j);
	            int l = 14737632;

	            if (!this.enabled)
	            {
	                l = -6250336;
	            }
	            else if (this.field_82253_i)
	            {
	                l = 16777120;
	            }

	            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, l);
	       
			}
		}
		@Override
		public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
			boolean fireButton = super.mousePressed(par1Minecraft, par2, par3);
			if(fireButton){
				clicked = true;
	            mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
	            ((ContainerProjectBenchII)inventorySlots).scrambleMatrix();
			}
			return fireButton;
		}
	}
	public class PBRefreshMatrixButton extends GuiButton{
		private boolean clicked = false;
		private int displayTicks = 0;
		public PBRefreshMatrixButton(int id, int xPos, int yPos, int width,
				int height, String label) {
			super(id, xPos, yPos, width, height, label);
		}
		
		@Override
		public void drawButton(Minecraft par1Minecraft, int i, int j) {
			if(this.drawButton){
				if(clicked){
					if(++displayTicks > 20){
						clicked = false;
						displayTicks = 0;
					}
				}
				FontRenderer fontrenderer = mc.fontRenderer;
	            mc.renderEngine.bindTexture(texPath);
	            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	            this.field_82253_i = i >= this.xPosition && j >= this.yPosition && i < this.xPosition + this.width && j < this.yPosition + this.height;
	            int k = this.getHoverState(this.field_82253_i);
	            //Top left, top right, bottom left, bottom right, icon
	            GL11.glPushMatrix();
	            this.drawTexturedModalRect(this.xPosition, this.yPosition, 176, 30 + (k-1)*9*(clicked ? 2:1), width, height);
	            GL11.glPopMatrix();
	            this.mouseDragged(mc, i, j);
	            int l = 14737632;
	            
	            if (!this.enabled)
	            {
	                l = -6250336;
	            }
	            else if (this.field_82253_i)
	            {
	                l = 16777120;
	            }

	            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, l);
	       
			}
		}
		
		@Override
		public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
			boolean fireButton = super.mousePressed(par1Minecraft, par2, par3);
			if(fireButton){
				clicked = true;
	            mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
	            ((ContainerProjectBenchII)inventorySlots).lookForOutputs();
			}
			return fireButton;
		}
	}
}