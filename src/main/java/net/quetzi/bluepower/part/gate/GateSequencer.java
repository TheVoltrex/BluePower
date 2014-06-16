package net.quetzi.bluepower.part.gate;

import java.util.Arrays;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.quetzi.bluepower.api.part.RedstoneConnection;
import net.quetzi.bluepower.client.gui.gate.GuiGateSingleTime;
import net.quetzi.bluepower.client.renderers.RenderHelper;
import net.quetzi.bluepower.part.IGuiButtonSensitive;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class GateSequencer extends GateBase implements IGuiButtonSensitive {
    
    private final boolean[] power = new boolean[4];
    private int             start = -1;
    private int             time  = 160;
    private int             ticks = 0;
    
    @Override
    public void initializeConnections(RedstoneConnection front, RedstoneConnection left, RedstoneConnection back, RedstoneConnection right) {
    
        // Init front
        front.enable();
        front.setOutput();
        
        // Init left
        left.enable();
        left.setOutput();
        
        // Init back
        back.enable();
        back.setOutput();
        
        // Init right
        right.enable();
        right.setOutput();
    }
    
    @Override
    public String getGateID() {
    
        return "sequencer";
    }
    
    @Override
    public void renderTop(RedstoneConnection front, RedstoneConnection left, RedstoneConnection back, RedstoneConnection right, float frame) {
    
        RenderHelper.renderRedstoneTorch(0, 1D / 8D, -5D / 16D, 8D / 16D, power[1]);
        RenderHelper.renderRedstoneTorch(5D / 16D, 1D / 8D, 0, 8D / 16D, power[2]);
        RenderHelper.renderRedstoneTorch(0, 1D / 8D, 5D / 16D, 8D / 16D, power[3]);
        RenderHelper.renderRedstoneTorch(-5D / 16D, 1D / 8D, 0, 8D / 16D, power[0]);
        
        RenderHelper.renderRedstoneTorch(0, 1D / 8D, 0, 13D / 16D, true);
        RenderHelper.renderPointer(0, 7D / 16D, 0, world != null ? start >= 0 ? 1 - (double) (ticks - start + frame) / (double) time : 0 : 0);
    }
    
    @Override
    public void doLogic(RedstoneConnection front, RedstoneConnection left, RedstoneConnection back, RedstoneConnection right) {
    
        Arrays.fill(power, false);
        
        if (start >= 0) {
            if (ticks >= start + time) {
                start = ticks;
            }
        } else {
            start = ticks;
        }
        
        double t = (double) (ticks - start) / (double) time;
        
        if (t >= 1D / 8D && t < 3D / 8D) {
            power[2] = true;
        } else if (t >= 3D / 8D && t < 5D / 8D) {
            power[3] = true;
        } else if (t >= 5D / 8D && t < 7D / 8D) {
            power[0] = true;
        } else if (t >= 7D / 8D && t < 1 || t >= 0 && t < 1D / 8D) {
            power[1] = true;
        }
        
        left.setPower(power[0] ? 15 : 0);
        front.setPower(power[1] ? 15 : 0);
        right.setPower(power[2] ? 15 : 0);
        back.setPower(power[3] ? 15 : 0);
        
        ticks++;
    }
    
    @Override
    public void save(NBTTagCompound tag) {
    
        super.save(tag);
        tag.setInteger("start", start);
        tag.setInteger("ticks", ticks);
        tag.setInteger("time", time);
    }
    
    @Override
    public void load(NBTTagCompound tag) {
    
        super.load(tag);
        start = tag.getInteger("start");
        ticks = tag.getInteger("ticks");
        time = tag.getInteger("time");
    }
    
    @Override
    public void onButtonPress(int messageId, int value) {
    
        time = value * 4;
        sendUpdatePacket();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getGui() {
    
        return new GuiGateSingleTime(this) {
            
            @Override
            protected int getCurrentIntervalTicks() {
            
                return time / 4;
            }
            
        };
    }

    @Override
    protected boolean hasGUI() {

        return true;
    }
}