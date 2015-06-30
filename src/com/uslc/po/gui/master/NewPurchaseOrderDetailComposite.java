package com.uslc.po.gui.master;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

import com.uslc.po.gui.logic.NewPurchaseOrderLogic;
import com.uslc.po.gui.master.NewPurchaseOrderComposite.PODetailData;
import com.uslc.po.gui.master.catalog.FormCenterMaster;
import com.uslc.po.gui.util.ImageUtils;
import com.uslc.po.jpa.entity.Color;
import com.uslc.po.jpa.entity.Item;
import com.uslc.po.jpa.entity.Size;
import com.uslc.po.jpa.entity.Upc;
import com.uslc.po.jpa.logic.UpcRepo;
import com.uslc.po.jpa.util.Constants;

public class NewPurchaseOrderDetailComposite extends FormCenterMaster {
	private Label titleLbl = null;
	private Label sizeLbl = null;
	private Combo sizeCbx = null;
	private Label colorLbl = null;
	private Combo colorCbx = null;
	private Label upcLbl = null;
	private Combo upcCbx = null;
	private Label imageLbl = null;
	private Label quantityLbl = null;
	private Text quantityTxt = null;
	private Label itemsByCartonLbl = null;
	private Text itemsByCartonTxt = null;
	private Label cartonsLbl = null;
	private Text cartonTxt = null;
	private Label preticketedLbl = null;
	private Button[] preticketedBtn = null;
	private Label createdLbl = null;
	private Label timestampLbl = null;
	private Button addBtn = null;
	private List<Upc> upcList = null;
	
	private GridData gdHorizontalDouble = null;
	private GridData gdLabel = null;
	private GridData gdText = null;
	
	private Logger log = null;
	
	public NewPurchaseOrderDetailComposite( MasterRightComposite composite ) {
		super( composite, SWT.NONE );
		
		initComposite();
	}
	
	private void initComposite() {
		//getLog().info( "initComposite" );
		
		GridData data = new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.HORIZONTAL_ALIGN_CENTER);
		data.grabExcessVerticalSpace=true;
		data.widthHint = 185;
		data.heightHint = 550;
		setLayoutData( data );
		
		layout();
		getParent().layout();
		getParent().getMaster().getShell().layout();
		
		GridLayout layout = new GridLayout( 2, false );
		setLayout( layout );
		
		getTitleLbl();
		getSizeLbl();
		getSizeCbx();
		getColorLbl();
		getColorCbx();
		getUpcLbl();
		getUpcCbx();
		getImageLbl();
		getQuantityLbl();
		getQuantityTxt();
		getItemsByCartonLbl();
		getItemsByCartonTxt();
		getCartonsLbl();
		getCartonTxt();
		getPreticketedLbl();
		getPreticketedBtn();
		getCreatedLbl();
		getTimestampLbl();
		getAddBtn();
		
		loadVaues();
	}
	
	public Label getTitleLbl() {
		if( titleLbl == null ) {
			//getLog().info( "getTitleLbl" );
			titleLbl = new Label(this, SWT.NONE);
			titleLbl.setText( "add order detail" );
			titleLbl.setAlignment(SWT.RIGHT);
			titleLbl.setLayoutData( getGdHorizontalDouble() );
			titleLbl.setFont( getMaster().getSystemVariables().getXSmallFont() );
			Label horizontalLine = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
			horizontalLine.setLayoutData( getGdHorizontalDouble() );
		}
		return titleLbl;
	}
	public Label getSizeLbl() {
		if( sizeLbl == null ) {
			sizeLbl = new Label(this, SWT.NONE);
			sizeLbl.setText( "size:" );
			sizeLbl.setAlignment(SWT.RIGHT);
			sizeLbl.setFont(getMaster().getSystemVariables().getXSmallFont());
			sizeLbl.setLayoutData(getGdLabel());
		}
		return sizeLbl;
	}
	public Combo getSizeCbx() {
		if( sizeCbx == null ) {
			sizeCbx = new Combo( this, SWT.READ_ONLY );
			sizeCbx.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			sizeCbx.addSelectionListener( new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					filterUpcs();
				}
			});
			sizeCbx.setLayoutData(getGdText());
			sizeCbx.setFont(getMaster().getSystemVariables().getXSmallFont());
		}
		return sizeCbx;
	}
	private void filterUpcs() {
		Color color = null;
		Size size = null;
		try{color = (Color)getColorCbx().getData(getColorCbx().getItem( getColorCbx().getSelectionIndex() ));}catch(Exception e){}
		try{size = (Size)getSizeCbx().getData(getSizeCbx().getItem( getSizeCbx().getSelectionIndex() ));}catch(Exception e){}
		
		List<Upc> tmpList = new ArrayList<Upc>();
		
		for ( Upc upc : getUpcList()) {
			if( color!=null && size!=null ) {
				if( upc.getColor().getId()==color.getId() && upc.getSize().getId()==size.getId() ) {
					tmpList.add(upc);
				}
			}else if( color!=null ) {
				if( upc.getColor().getId()==color.getId() ) {
					tmpList.add(upc);
				}
			}else if( size!=null ) {
				if( upc.getSize().getId()==size.getId() ) {
					tmpList.add(upc);
				}
			}
			getUpcCbx().removeAll();
			if( tmpList.size()>0 ){
				for (Upc u : tmpList) {
					getUpcCbx().add( u.getUpcCode() );
					getUpcCbx().setData(u.getUpcCode(), u);
				}
				//getLog().info( "fillUpcCombo.getUpcCbx().getItemCount(): " + getUpcCbx().getItemCount() );
				getUpcCbx().select(-1);
			}
		}
	}
	public Label getColorLbl() {
		if( colorLbl == null ) {
			colorLbl = new Label(this, SWT.NONE);
			colorLbl.setText( "color:" );
			colorLbl.setAlignment(SWT.RIGHT);
			colorLbl.setLayoutData(getGdLabel());
			colorLbl.setFont(getMaster().getSystemVariables().getXSmallFont());
		}
		return colorLbl;
	}
	public Combo getColorCbx() {
		if( colorCbx == null ) {
			colorCbx = new Combo( this, SWT.READ_ONLY );
			colorCbx.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			colorCbx.addSelectionListener( new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					filterUpcs();
				}
			});
			colorCbx.setLayoutData(getGdText());
			colorCbx.setFont(getMaster().getSystemVariables().getXSmallFont());
		}
		return colorCbx;
	}
	public Label getUpcLbl() {
		if( upcLbl == null ){
			upcLbl = new Label(this, SWT.NONE);
			upcLbl.setText( "upc:" );
			upcLbl.setLayoutData(getGdLabel());
			upcLbl.setFont(getMaster().getSystemVariables().getXSmallFont());
		}
		return upcLbl;
	}
	public Combo getUpcCbx() {
		if( upcCbx == null ){
			upcCbx = new Combo( this, SWT.READ_ONLY );
			upcCbx.setLayoutData(getGdHorizontalDouble());
			upcCbx.addSelectionListener( new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					updateUpcImage();
				}
			});
			upcCbx.setFont(getMaster().getSystemVariables().getXSmallFont());
		}
		return upcCbx;
	}
	public Label getImageLbl() {
		if( imageLbl == null ){
			imageLbl = new Label( this, SWT.IMAGE_PNG );
			GridData gd = new GridData(150, 60);
			gd.horizontalSpan = 2;
			gd.horizontalAlignment = SWT.CENTER;
			imageLbl.setLayoutData(gd);
			
			Label horizontalLine = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
			horizontalLine.setLayoutData( getGdHorizontalDouble() );
		}
		return imageLbl;
	}
	public Label getQuantityLbl() {
		if( quantityLbl == null ){
			quantityLbl = new Label(this, SWT.NONE);
			quantityLbl.setText( "qty:" );
			quantityLbl.setAlignment(SWT.RIGHT);
			quantityLbl.setLayoutData(getGdLabel());
			quantityLbl.setFont(getMaster().getSystemVariables().getXSmallFont());
		}
		return quantityLbl;
	}
	public Text getQuantityTxt() {
		if( quantityTxt == null ){
			quantityTxt = new Text(this, SWT.NONE);
			quantityTxt.setLayoutData(getGdText());
			quantityTxt.addModifyListener( new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent arg0) {
					updateCartonTotal();
				}
			});
			quantityTxt.setFont(getMaster().getSystemVariables().getXSmallFont());
		}
		return quantityTxt;
	}
	public Label getItemsByCartonLbl() {
		if( itemsByCartonLbl == null ){
			itemsByCartonLbl = new Label(this, SWT.NONE);
			itemsByCartonLbl.setText( "item/c:" );
			itemsByCartonLbl.setAlignment(SWT.RIGHT);
			itemsByCartonLbl.setLayoutData(getGdLabel());
			itemsByCartonLbl.setFont(getMaster().getSystemVariables().getXSmallFont());
		}
		return itemsByCartonLbl;
	}
	public Text getItemsByCartonTxt() {
		if( itemsByCartonTxt == null ) {
			itemsByCartonTxt = new Text(this, SWT.NONE);
			itemsByCartonTxt.setLayoutData(getGdText());
			itemsByCartonTxt.addModifyListener( new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent arg0) {
					updateCartonTotal();
				}
			});
			itemsByCartonTxt.setFont(getMaster().getSystemVariables().getXSmallFont());
		}
		return itemsByCartonTxt;
	}
	public Label getCartonsLbl() {
		if( cartonsLbl == null ) {
			cartonsLbl = new Label(this, SWT.NONE);
			cartonsLbl.setText( "cartons:" );
			cartonsLbl.setAlignment(SWT.RIGHT);
			cartonsLbl.setLayoutData(getGdLabel());
			cartonsLbl.setFont(getMaster().getSystemVariables().getXSmallFont());
		}
		return cartonsLbl;
	}
	public Text getCartonTxt() {
		if( cartonTxt == null ) {
			cartonTxt = new Text(this, SWT.READ_ONLY);
			cartonTxt.setLayoutData(getGdText());
			cartonTxt.setText(String.valueOf( getMaster().getCommonPurchaseOrderValues().getDefaultItemsByCarton() ) );
			cartonTxt.setFont(getMaster().getSystemVariables().getXSmallFont());
		}
		return cartonTxt;
	}
	public Label getPreticketedLbl() {
		if( preticketedLbl == null ) {
			preticketedLbl = new Label(this, SWT.NONE);
			preticketedLbl.setText( "preticketed:" );
			preticketedLbl.setLayoutData(getGdHorizontalDouble());
			preticketedLbl.setFont(getMaster().getSystemVariables().getXSmallFont());
		}
		return preticketedLbl;
	}
	public Button[] getPreticketedBtn() {
		if( preticketedBtn == null ) {
			preticketedBtn = new Button[2];
			
			GridData gd = new GridData( 50, 23 );
			
			preticketedBtn[0] = new Button(this, SWT.RADIO | SWT.CENTER );
			preticketedBtn[0].setSelection(false);
			preticketedBtn[0].setText( "Y" );
			preticketedBtn[0].setLayoutData(gd);
			preticketedBtn[0].setFont(getMaster().getSystemVariables().getXSmallFont());
			
			preticketedBtn[1] = new Button(this, SWT.RADIO | SWT.CENTER );
			preticketedBtn[1].setSelection(false);
			preticketedBtn[1].setText( "N" );
			preticketedBtn[1].setLayoutData(gd);
			preticketedBtn[1].setFont(getMaster().getSystemVariables().getXSmallFont());
		}
		return preticketedBtn;
	}
	public Label getCreatedLbl() {
		if( createdLbl == null ) {
			createdLbl = new Label(this, SWT.NONE);
			createdLbl.setText( "created:" );
			createdLbl.setLayoutData(getGdHorizontalDouble());
			createdLbl.setFont(getMaster().getSystemVariables().getXSmallFont());
		}
		return createdLbl;
	}
	public Label getTimestampLbl() {
		if( timestampLbl == null ) {
			timestampLbl = new Label(this, SWT.NONE);
			timestampLbl.setText( "2014-01-15 15:09:23" );
			timestampLbl.setLayoutData(getGdHorizontalDouble());
			timestampLbl.setAlignment(SWT.RIGHT);
			timestampLbl.setFont(getMaster().getSystemVariables().getXSmallFont());
			
			Label horizontalLine = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
			horizontalLine.setLayoutData( getGdHorizontalDouble() );
		}
		return timestampLbl;
	}
	public Button getAddBtn() {
		if( addBtn == null ){
			addBtn = new Button(this, SWT.PUSH);
			addBtn.setText("add detail");
			addBtn.setAlignment(SWT.CENTER);
			GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
			gd.grabExcessHorizontalSpace=true;
			gd.heightHint = 20;
			gd.widthHint = 100;
			gd.horizontalSpan=2;
			addBtn.setLayoutData(gd);
			addBtn.setFont(getMaster().getSystemVariables().getXSmallFont());
			addBtn.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					addPoDetail();
				}
			});
			
			Label horizontalLine = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
			horizontalLine.setLayoutData( getGdHorizontalDouble() );
		}
		return addBtn;
	}

	public GridData getGdHorizontalDouble() {
		if( gdHorizontalDouble == null ) {
			gdHorizontalDouble = new GridData( SWT.FILL, SWT.FILL, true, false );
			gdHorizontalDouble.horizontalSpan = 2;
		}
		return gdHorizontalDouble;
	}
	public GridData getGdLabel(){
		if( gdLabel == null ){
			gdLabel = new GridData();
			gdLabel.widthHint=65;
		}
		return gdLabel;
	}
	public GridData getGdText(){
		if( gdText == null ){
			gdText = new GridData();
			gdText.widthHint=100;
			gdText.heightHint=23;
		}
		return gdText;
	}

	private void clean(){
		getUpcCbx().select(-1);
		getQuantityTxt().setText("0");
		getItemsByCartonTxt().setText(String.valueOf( getMaster().getCommonPurchaseOrderValues().getDefaultItemsByCarton() ));
		getPreticketedBtn()[0].setSelection(false);
		getPreticketedBtn()[1].setSelection(false);
	}
	public void hide(){
		clean();
		this.setParent( getParent().getMaster().getHiddenShell() );
		this.setVisible(false);
		((MasterRightComposite)getParent()).getInfoComposite().moveAbove(this);
		moveBelow(((MasterRightComposite)getParent()).getInfoComposite());
		layout();
		getParent().layout();
	}
	public void loadVaues(){
		getUpcCbx().removeAll();
		getColorCbx().removeAll();
		getSizeCbx().removeAll();
		upcList = null;
		if( getSelectedItem()!=null ){
			fillUpcCombo();
			fillColorCombo();
			fillSizeCombo();
		}
	}
	private void fillUpcCombo(){
		if( getUpcList()!=null ){
			for (Upc upc : getUpcList()) {
				getUpcCbx().add( upc.getUpcCode() );
				getUpcCbx().setData(upc.getUpcCode(), upc);
			}
			getLog().info( "fillUpcCombo.getUpcCbx().getItemCount(): " + getUpcCbx().getItemCount() );
			getUpcCbx().select(-1);
		}
	}
	private void fillColorCombo(){
		if( getUpcList()!=null ){
			HashMap<String,Color> colors = new HashMap<String,Color>();
			for (Upc upc : getUpcList()) {
				colors.put( upc.getColor().getName(), upc.getColor() );
			}
			
			Iterator<Entry<String,Color>> itColor = colors.entrySet().iterator();
			while( itColor.hasNext() ){
				Map.Entry<String,Color> entry = itColor.next();
				getColorCbx().add( entry.getKey() );
				getColorCbx().setData(entry.getKey(), entry.getValue());
			}
		}
	}
	private void fillSizeCombo(){
		if( getUpcList()!=null ){
			HashMap<String,Size> sizes = new HashMap<String,Size>();
			for (Upc upc : getUpcList()) {
				if( getMaster().getMasterCenter().getNewPurchaseOrder().getOrderNumberTxt().getText().endsWith("11") ){
					sizes.put( String.valueOf(upc.getSize().getWaist()), upc.getSize() );
				}else{
					sizes.put( upc.getSize().getWaist()+"x"+upc.getSize().getInseam(), upc.getSize() );
				}
			}
			
			Iterator<Entry<String,Size>> itSize = sizes.entrySet().iterator();
			while( itSize.hasNext() ){
				Map.Entry<String,Size> entry = itSize.next();
				getSizeCbx().add( entry.getKey() );
				getSizeCbx().setData(entry.getKey(), entry.getValue());
			}
		}
	}
	private void addPoDetail(){
		Upc upc = null;
		int qty = 0;
		int itemsPerCarton = 0;
		
		try{
			upc = (Upc)getUpcCbx().getData( getUpcCbx().getItem( getUpcCbx().getSelectionIndex() ) );
			qty = Integer.parseInt( getQuantityTxt().getText() );
			itemsPerCarton = Integer.parseInt( getItemsByCartonTxt().getText() );
			Combo itemCombo = getMaster().getMasterCenter().getNewPurchaseOrder().getItemsCbx(); 
		}catch( Exception e ){
			MessageBox box = new MessageBox(getShell() , SWT.ICON_ERROR);
			box.setText( Constants.MESSAGE_BOX_DIAG_TITLE.toString() );
			box.setMessage( "Check the detail values." );
			box.open();
			return;
		}
		
		if( qty>0 && itemsPerCarton>0 ){
			boolean preticketed = false;
			
			
			if( getPreticketedBtn()[0].getSelection()!=getPreticketedBtn()[1].getSelection() ){
				preticketed = getPreticketedBtn()[0].getSelection();
			}else{
				MessageBox box = new MessageBox(getShell() , SWT.ICON_ERROR);
				box.setText( Constants.MESSAGE_BOX_DIAG_TITLE.toString() );
				box.setMessage( "Please select if the items are preticketed or not." );
				box.open();
				return;
			}
			
			PODetailData poData = getMaster().getMasterCenter().getNewPurchaseOrder().new PODetailData(upc, qty, itemsPerCarton, preticketed);
			
			if( ((NewPurchaseOrderLogic)getMaster().getMasterCenter().getNewPurchaseOrder().getLiveDataAccessLifeCicle()).addPoDetail(poData) ){
				clean();
			}else{
				MessageBox box = new MessageBox(getShell() , SWT.ICON_INFORMATION);
				box.setText( Constants.MESSAGE_BOX_DIAG_TITLE.toString() );
				box.setMessage( "There was a problem while adding the purchase order detail" );
				box.open();
				//clean();
			}
		}else{
			MessageBox box = new MessageBox(getShell() , SWT.ICON_INFORMATION);
			box.setText( Constants.MESSAGE_BOX_DIAG_TITLE.toString() );
			box.setMessage( "Please check the correct amounts." );
			box.open();
		}
		
	}
	private void updateCartonTotal(){
		int totalItems = 0;
		int itemsPerCarton = 0;
		int cartons = 0;
		try{ totalItems = (int)Double.parseDouble( getQuantityTxt().getText() ); }catch(Exception e){}
		try{ itemsPerCarton = (int)Double.parseDouble( getItemsByCartonTxt().getText() ); }catch(Exception e){}
		if( itemsPerCarton>0 ){
			try{ cartons = (int)Math.ceil( new Double(totalItems)/new Double(itemsPerCarton) ); }catch(Exception e){}
		}
		getCartonTxt().setText( String.valueOf( cartons ) );
	}
	private void updateUpcImage(){
		Upc upc = (Upc)getUpcCbx().getData( getUpcCbx().getItem( getUpcCbx().getSelectionIndex() ) );
		
		getImageLbl().setImage(null);
		if( upc!=null ){
			Image ucpImage = ImageUtils.getBarcodeImage(getDisplay(), upc.getUpcCode());
			getImageLbl().setImage(ucpImage);
		}
	}
	private Logger getLog(){
		if( log == null ){
			log = Logger.getLogger(NewPurchaseOrderDetailComposite.class);
			PropertyConfigurator.configure("log4j.properties");
		}
		return log;
	}
	private Item getSelectedItem(){
		Item item = null;
		Combo cbx = getMaster().getMasterCenter().getNewPurchaseOrder().getItemsCbx();
		try{
			item = (Item)cbx.getData(cbx.getItem(cbx.getSelectionIndex()));
		}catch( Exception e ){
			getLog().error(e);
		}
		return item;
	}
	public List<Upc> getUpcList(){
		if( upcList==null ){
			if( getMaster().getMasterCenter().getNewPurchaseOrder().getOrderNumberTxt().getText().endsWith( "11" ) ){
				upcList = UpcRepo.findByItemAndInseam(getSelectedItem(), 36 );
			}else{
				upcList = UpcRepo.findByItem(getSelectedItem());
			}
			getLog().info( "getUpcList.upcList.size(): " + upcList.size() );
		}
		return upcList;
	}
	
}
