package moze_intel.gameObjs.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import moze_intel.gameObjs.ObjHandler;
import moze_intel.gameObjs.container.inventory.AlchBagInventory;
import moze_intel.utils.Utils;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;


public class LootBall extends Entity
{
	private final int lifespan = 6000;
	private List<ItemStack> items;
	private int age;
	private float hoverStart;
	
	public LootBall(World world)
	{
		super(world);
		this.hoverStart = (float)(Math.random() * Math.PI * 2.0D);
        this.setSize(0.25F, 0.25F);
        this.yOffset = this.height / 2.0F;
	}
	
	public LootBall(World world, ItemStack[] drops, double x, double y, double z)
	{
		super(world);
		items = Arrays.asList(drops);
		
		this.setSize(0.25F, 0.25F);
		this.yOffset = this.height / 2.0F;
		this.setPosition(x, y, z);
		this.hoverStart = (float)(Math.random() * Math.PI * 2.0D);
		this.motionX = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D));
        this.motionY = 0.20000000298023224D;
        this.motionZ = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D));
	}
	
	public LootBall(World world, List<ItemStack> drops, double x, double y, double z)
	{
		super(world);
		items = drops;
		
		this.setSize(0.25F, 0.25F);
		this.yOffset = this.height / 2.0F;
		this.setPosition(x, y, z);
		this.hoverStart = (float)(Math.random() * Math.PI * 2.0D);
		this.motionX = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D));
        this.motionY = 0.20000000298023224D;
        this.motionZ = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D));
	}
	
	public List<ItemStack> getItemList()
	{
		return items;
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		 this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         this.motionY -= 0.03999999910593033D;
         this.noClip = this.func_145771_j(this.posX, (this.boundingBox.minY + this.boundingBox.maxY) / 2.0D, this.posZ);
         this.moveEntity(this.motionX, this.motionY, this.motionZ);
         boolean flag = (int)this.prevPosX != (int)this.posX || (int)this.prevPosY != (int)this.posY || (int)this.prevPosZ != (int)this.posZ;
         
         
         if (flag || this.ticksExisted % 25 == 0)
         {
             if (this.worldObj.getBlock(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)).getMaterial() == Material.lava)
             {
                 this.motionY = 0.20000000298023224D;
                 this.motionX = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
                 this.motionZ = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
                 this.playSound("random.fizz", 0.4F, 2.0F + this.rand.nextFloat() * 0.4F);
             }
         }
         
         float f = 0.98F;

         if (this.onGround)
             f = this.worldObj.getBlock(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.boundingBox.minY) - 1, MathHelper.floor_double(this.posZ)).slipperiness * 0.98F;

         this.motionX *= (double)f;
         this.motionY *= 0.9800000190734863D;
         this.motionZ *= (double)f;

         if (this.onGround)
             this.motionY *= -0.5D;

         ++this.age;

		
		if (!this.worldObj.isRemote)
		{
			if (age > lifespan)
				this.setDead();
		}
	}
	
	@Override
	public void onCollideWithPlayer(EntityPlayer player)
	{
		if (!this.worldObj.isRemote)
		{
			boolean playSound = false;
			List<ItemStack> list = new ArrayList();
			ItemStack bag = getAlchemyBag(player.inventory.mainInventory);
			
			if (bag != null && Utils.invContainsItem(new AlchBagInventory(bag), new ItemStack(ObjHandler.blackHole, 1, 1)))
			{
				for (ItemStack stack : items)
				{
					ItemStack remaining = Utils.pushStackInInv(new AlchBagInventory(bag), stack);
					
					if (remaining == null) 
					{
						if (!playSound)
						{
							playSound = true;
						}
							
						continue;
					}
					else
					{
						remaining = Utils.pushStackInInv(player.inventory, remaining);
						
						if (remaining == null)
						{
							if (!playSound)
							{
								playSound = true;
							}
								
							continue;
						}
						else
						{
							list.add(remaining);
						}
						
						if (!playSound && !Utils.areItemStacksEqual(stack, remaining))
						{
							playSound = true;
						}
					}
					
				}
			}
			else
			{
				for (ItemStack stack : items)
				{
					ItemStack remaining = Utils.pushStackInInv(player.inventory, stack);
				
					if (remaining == null) 
					{
						if (!playSound)
						{
							playSound = true;
						}
					
						continue;
					}
					else
					{
						list.add(remaining);
					}
				
					if (!playSound && !Utils.areItemStacksEqual(stack, remaining))
					{
						playSound = true;
					}
				}
			}
			
			if (playSound)
			{
				this.worldObj.playSoundAtEntity(player, "random.pop", 0.2F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
			}
		
			if (list.size() > 0)
			{
				items = list;
			}
			else
			{
				this.setDead();
			}
		}
	}
	
	private ItemStack getAlchemyBag(ItemStack[] inventory)
	{
		for (ItemStack stack : inventory)
		{
			if (stack == null) 
			{
				continue;
			}
			
			if (stack.getItem() == ObjHandler.alchBag && Utils.invContainsItem(new AlchBagInventory(stack), new ItemStack(ObjHandler.blackHole, 1, 1)))
			{
				return stack;
			}
		}
		return null;
	}
	
	@Override
	public boolean handleWaterMovement()
    {
        return this.worldObj.handleMaterialAcceleration(this.boundingBox, Material.water, this);
    }

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) 
	{
		age = nbt.getShort("Age");
		items = new ArrayList();
		
		NBTTagList list = nbt.getTagList("Items", 10);
		for (int i = 0; i < list.tagCount(); i++)
		{
			NBTTagCompound listTag = list.getCompoundTagAt(i);
			items.add(ItemStack.loadItemStackFromNBT(listTag));
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) 
	{
		nbt.setShort("Age", (short)this.age);
		
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < items.size(); i++)
		{
			NBTTagCompound subNBT = new NBTTagCompound();
			items.get(i).writeToNBT(subNBT);
			list.appendTag(subNBT);
		}
		nbt.setTag("Items", list);
	}
	
	@Override
	protected boolean canTriggerWalking()
	{
		return false;
	}

	@Override
	protected void entityInit() {}
}
