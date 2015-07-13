package com.uslc.po.gui.master;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.uslc.po.gui.master.catalog.FormCenterMaster.InfoForm;
import com.uslc.po.gui.master.interfaces.LiveDataAccessLifeCicle;

public class MasterRightComposite extends MasterSections {
	private NewPurchaseOrderDetailComposite poDetail = null;
	private Group infoComposite = null;
	private Label titleLbl = null;
	private Label descLbl = null;
	private Label featuresLbl = null;
	private Logger log = null;
	
	private LiveDataAccessLifeCicle ldalc = null;
	
	public MasterRightComposite( Master master ){
		super( master, SWT.BORDER );
		initComposite();
		getLog().info( MasterRightComposite.class + " constructor has been called!" );
	}
	
	protected void initComposite(){
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, true );
		data.widthHint = 200;
		setLayoutData( data );
		
		GridLayout layout = new GridLayout();
		layout.marginTop = 20;
		setLayout(layout);
		getInfoComposite();
	}

	public NewPurchaseOrderDetailComposite getNewPurchaseOrderDetail() {
		if( poDetail == null ){
			poDetail = new NewPurchaseOrderDetailComposite(this);
		}
		return poDetail;
	}
	public Group getInfoComposite(){
		if( infoComposite == null ){
			infoComposite = new Group(this, SWT.NONE);
			infoComposite.setText("info");
			GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true );
			infoComposite.setLayoutData(gd);
			GridLayout gl = new GridLayout();
			gl.numColumns=1;
			infoComposite.setLayout(gl);
			
			getTitleLbl();
			getDescLbl();
			getFeaturesLbl();
		}
		return infoComposite;
	}
	public void hideAllComposites(){
		try{getNewPurchaseOrderDetail().hide();}catch(Exception e){e.printStackTrace();}
	}
	public void showComposite( Composite composite ){
		hideAllComposites();
		
		composite.setParent( this );
		composite.moveAbove(getInfoComposite());
		getInfoComposite().moveBelow(composite);
		composite.setVisible( true );
		composite.layout();
		this.layout();
		getMaster().getShell().layout();
	}
	public Label getTitleLbl(){
		if( titleLbl == null ){
			titleLbl = new Label(getInfoComposite(), SWT.NONE );
			titleLbl.setAlignment( SWT.CENTER );
			Font f = null;
			for(FontData fd : titleLbl.getFont().getFontData() ) {
				f = new Font( getDisplay(), fd.getName(), fd.getHeight(), SWT.NORMAL );
			}
			titleLbl.setFont( f );
			GridData gd = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.verticalAlignment = GridData.BEGINNING;
			gd.grabExcessHorizontalSpace=true;
			gd.grabExcessVerticalSpace=false;
			titleLbl.setLayoutData(gd);
		}
		return titleLbl;
	}
	public Label getDescLbl(){
		if( descLbl == null ){
			descLbl = new Label( getInfoComposite(), SWT.WRAP );
			Font f = null;
			for( FontData fd : descLbl.getFont().getFontData() ) {
				f = new Font(getDisplay(), fd.getName(), fd.getHeight()-2, fd.getStyle() );
			}
			descLbl.setFont(f);
			GridData gd = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.verticalAlignment = GridData.BEGINNING;
			gd.grabExcessHorizontalSpace=true;
			gd.grabExcessVerticalSpace=false;
			descLbl.setLayoutData(gd);
		}
		return descLbl;
	}
	public Label getFeaturesLbl(){
		if( featuresLbl == null ){
			featuresLbl = new Label( getInfoComposite(), SWT.WRAP );
			Font f = null;
			for( FontData fd : featuresLbl .getFont().getFontData() ) {
				f = new Font(getDisplay(), fd.getName(), fd.getHeight()-2, fd.getStyle() );
			}
			featuresLbl .setFont(f);
			GridData gd = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.verticalAlignment = GridData.BEGINNING;
			gd.grabExcessHorizontalSpace=true;
			gd.grabExcessVerticalSpace=false;
			featuresLbl.setLayoutData(gd);
		}
		return featuresLbl;
	}
	public void setInfoText( InfoForm info ){
		if( info == null ){
			getTitleLbl().setText( "" );
			getTitleLbl().getParent().layout();
			getDescLbl().setText( "" );
			getDescLbl().getParent().layout();
			getFeaturesLbl().setText( "" );
			getFeaturesLbl().getParent().layout();
		}else{
			getTitleLbl().setText( info.getTitle() );
			getTitleLbl().getParent().layout();
			getDescLbl().setText( info.getDesc() );
			getDescLbl().getParent().layout();
			String features = "";
			if( info.getFeatures() !=null ){
				for( String s : info.getFeatures() ){
					features += "- " + s + "\n";
				}
			}
			getFeaturesLbl().setText( features );
			getFeaturesLbl().getParent().layout();
		}
	}

	private Logger getLog() {
		if( log == null ) {
			log = Logger.getLogger( MasterRightComposite.class );
			PropertyConfigurator.configure( "log4j.properties" );
		}
		return log;
	}

	@Override
	public LiveDataAccessLifeCicle getLiveDataAccessLifeCicle() {
		if( ldalc == null ) {
			ldalc = new MasterRightCompositeLogic();
		}
		return ldalc;
	}
	
	public class MasterRightCompositeLogic implements LiveDataAccessLifeCicle {

		@Override
		public void displayValues() {
			
		}

		@Override
		public void clean() {
			
		}

		@Override
		public void refreshFormData() {
			getInfoComposite().layout();
			layout();
		}
		
	}
}