package com.uslc.po.gui.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporterParameter;
import net.sf.jasperreports.engine.util.JRFontNotFoundException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.jasperassistant.designer.viewer.ViewerComposite;
import com.uslc.po.gui.logic.ClientLogic;
import com.uslc.po.gui.util.ImageUtils;
import com.uslc.po.gui.util.Master;
import com.uslc.po.gui.util.PrintingUtils;
import com.uslc.po.jpa.comparator.PackageDetailComparator;
import com.uslc.po.jpa.entity.Carton;
import com.uslc.po.jpa.entity.PackingDetail;
import com.uslc.po.jpa.entity.PurchaseOrder;
import com.uslc.po.jpa.entity.PurchaseOrderDetail;
import com.uslc.po.jpa.entity.User;

import org.eclipse.swt.custom.ScrolledComposite;

public class POClient extends Shell {
	private Master master = null;
	private User user = null;
	private ClientLogic client = null;
	private Table poDetailTbl;
	private Table packingDetailTbl;
	private Combo purchaseOrderCbx;
	private Logger log = null;
	private Combo sizeCbx;
	private Combo colorCbx;
	private Combo itemCbx;
	private Table scannedItemsTbl;
	private Text scannedBarTxt;
	
	private ViewerComposite polybagViewer = null;
	private ViewerComposite ticketViewer = null;
	
	public ViewerComposite getPolybagViewer(){
		if( polybagViewer == null ){
			polybagViewer = new ViewerComposite( getGrpTicketsComposite(), SWT.NONE);
			GridData gd_ticketViewer = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
			gd_ticketViewer.heightHint = 150;
			gd_ticketViewer.widthHint = 200;
			polybagViewer.setLayoutData(gd_ticketViewer);
		}
		return polybagViewer;
	}
	public ViewerComposite getTicketViewer(){
		if( ticketViewer == null ){
			Composite printComposite = new Composite(grpTicketsComposite, SWT.NONE);
			printComposite.setLayout(new GridLayout(1, false));
			GridData gd_printComposite = new GridData(SWT.RIGHT, SWT.FILL, true, true, 1, 1);
			gd_printComposite.widthHint = 90;
			printComposite.setLayoutData(gd_printComposite);
			
			Button btnPrintSmall = new Button(printComposite, SWT.NONE);
			btnPrintSmall.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
			btnPrintSmall.setText("print small");
			
			Button btnPrintTicket = new Button(printComposite, SWT.NONE);
			btnPrintTicket.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					getClient().printTicket();
				}
			});
			btnPrintTicket.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
			btnPrintTicket.setText("print ticket");
			ticketViewer = new ViewerComposite( getGrpTicketsComposite(), SWT.NONE);
			GridData gd_polybagViewer = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
			gd_polybagViewer.heightHint = 200;
			gd_polybagViewer.widthHint = 300;
			ticketViewer.setLayoutData(gd_polybagViewer);
		}
		return ticketViewer;
	}
	
	private JasperPrint jasperPrintTicket = null;
	private JasperPrint jasperPrintPolybag = null;
	
	public JasperPrint getJasperPrintTicket( PackingDetail pd ) throws JRException{
		HashMap hm = new HashMap();
		if( pd==null ){
			jasperPrintTicket = JasperFillManager.fillReport(getJasperReportBlankTicket(),hm,new JREmptyDataSource());
		}else{
			hm.put( "carton" , String.valueOf( pd.getSku() ) );
			//hm.put( "barcode_number" , pd.getPurchaseOrderDetail().getUpc().getUpcCode() );
			
			//hm.put( "barcode" , ImageUtils.createResizedCopy( ImageUtils.getAwtBarcodeImage( pd.getPurchaseOrderDetail().getUpc().getUpcCode() ), 105, 81, true) );
			hm.put( "barcode" , ImageUtils.getAwtBarcodeImage( pd.getPurchaseOrderDetail().getUpc().getUpcCode() ) );
			hm.put( "dept" , String.valueOf( pd.getPurchaseOrderDetail().getPurchaseOrder().getDepartmentNumber() ) );
			hm.put( "item" , String.valueOf( pd.getPurchaseOrderDetail().getUpc().getItem().getCode() ) );
			hm.put( "color_number" , String.valueOf( pd.getPurchaseOrderDetail().getUpc().getColor().getNumber() ) );
			String size = "";
			if( pd.getPurchaseOrderDetail().getPurchaseOrder().getReferenceNumber().endsWith( "0011" ) ){
				size = String.valueOf( pd.getPurchaseOrderDetail().getUpc().getSize().getWaist() );
			}else{
				size = String.valueOf( pd.getPurchaseOrderDetail().getUpc().getSize().getWaist() + "x" + pd.getPurchaseOrderDetail().getUpc().getSize().getInseam() );
			}
			hm.put( "size" , size );
			hm.put( "color_name" , pd.getPurchaseOrderDetail().getUpc().getColor().getName() );
			hm.put( "po_number" , pd.getPurchaseOrderDetail().getPurchaseOrder().getReferenceNumber() );
			
			hm.put("qty", String.valueOf( ClientLogic.getNumberOfScannedItems( pd.getCarton() ) ) );
			try {
				hm.put("country", getMaster().getCountryOfOrigin());
			} catch (IOException e) {
				hm.put("country", "Nicaragua");
			}
			hm.put("to", pd.getPurchaseOrderDetail().getPurchaseOrder().getShipTo() );
			hm.put("from", pd.getPurchaseOrderDetail().getPurchaseOrder().getShipFrom() );
			
			try{
				jasperPrintTicket = JasperFillManager.fillReport(pd.getPurchaseOrderDetail().getPreticketed()?getJasperReportPreticketed():getJasperReportNoPreticketed(),hm,new JREmptyDataSource());
				if( jasperPrintTicket.getPages().size()>1 )jasperPrintTicket.getPages().remove(1);
				getLog().info( "number of pages[" + jasperPrintTicket.getPages().size() + "] - topMargin[" + jasperPrintTicket.getTopMargin() + "]" );
			}catch( JRFontNotFoundException e ){
				e.printStackTrace();
			}
			getLog().info( "jasperPrint[ " + jasperPrintTicket + " ]" );
			
		}
		return jasperPrintTicket;
	}
	public JasperPrint getJasperPrintPolybag( PackingDetail pd ) throws JRException {
		HashMap hm = new HashMap();
		if( pd==null ){
			jasperPrintPolybag = JasperFillManager.fillReport(getJasperReportBlankPolybag(),  hm, new JREmptyDataSource());
		}else{
			hm.put( "item" , String.valueOf( pd.getPurchaseOrderDetail().getUpc().getItem().getCode() ) );
			hm.put( "department" , String.valueOf( pd.getPurchaseOrderDetail().getPurchaseOrder().getDepartmentNumber() ) );
			hm.put( "color" , String.valueOf( pd.getPurchaseOrderDetail().getUpc().getColor().getNumber() ) );
			String size = "";
			if( pd.getPurchaseOrderDetail().getPurchaseOrder().getReferenceNumber().endsWith( "0011" ) ){
				size = String.valueOf( pd.getPurchaseOrderDetail().getUpc().getSize().getWaist() );
			}else{
				size = String.valueOf( pd.getPurchaseOrderDetail().getUpc().getSize().getWaist() + "x" + pd.getPurchaseOrderDetail().getUpc().getSize().getInseam() );
			}
			hm.put( "size" , size );
			hm.put( "color_name" , pd.getPurchaseOrderDetail().getUpc().getColor().getName() );
			hm.put( "upc_image" , ImageUtils.getAwtBarcodeImage( pd.getPurchaseOrderDetail().getUpc().getUpcCode() ) );
			
			try{
				jasperPrintPolybag = JasperFillManager.fillReport(getJasperReportPolybag(),hm,new JREmptyDataSource());
				if( jasperPrintPolybag.getPages().size()>1 )jasperPrintPolybag.getPages().remove(1);
				getLog().info( "number of pages[" + jasperPrintPolybag.getPages().size() + "] - topMargin[" + jasperPrintPolybag.getTopMargin() + "]" );
			}catch( JRFontNotFoundException e ){
				e.printStackTrace();
			}
		}
		getLog().info( "jasperPrintPolybag[ " + jasperPrintPolybag + " ]" );
		return jasperPrintPolybag;
	}
	
	private JasperReport noPreticketed = null;
	private JasperReport preticketed = null;
	private JasperReport polybag = null;
	
	public JasperReport getJasperReportPreticketed() throws JRException{
		if( preticketed == null ){
			try {
				preticketed = JasperCompileManager.compileReport( getMaster().getPreticketedReportPath() );
			} catch (IOException e) {
				getLog().info( "error", e );
			}
		}
		return preticketed;
	}
	public JasperReport getJasperReportNoPreticketed() throws JRException{
		if( noPreticketed == null ){
			try {
				noPreticketed = JasperCompileManager.compileReport( getMaster().getNoPreticketedReportPath() );
			} catch (IOException e) {
				getLog().info( "error", e );
			}
		}
		return noPreticketed;
	}
	public JasperReport getJasperReportPolybag() throws JRException{
		if( polybag == null ){
			try {
				polybag = JasperCompileManager.compileReport(getMaster().getStickerReportPath() );
			} catch (IOException e) {
				getLog().info( "error", e );
			}
		}
		return polybag;
	}
	
	private JasperReport blankTicket = null;
	private JasperReport blankPolybag = null;
	public JasperReport getJasperReportBlankPolybag() throws JRException{
		if( blankPolybag == null ){
			try {
				blankPolybag = JasperCompileManager.compileReport(getMaster().getBlankStickerReportPath() );
			} catch (IOException e) {
				getLog().info( "error", e );
			}
		}
		return blankPolybag;
	}
	public JasperReport getJasperReportBlankTicket() throws JRException{
		if( blankTicket == null ){
			try {
				blankTicket = JasperCompileManager.compileReport(getMaster().getBlanckPreticketedPath() );
			} catch (IOException e) {
				getLog().info( "error", e );
			}
		}
		return blankTicket;
	}
	
	private int poDetailQty = 0;
	
	private Group grpTicketsComposite;
	private Combo printersCbx;
	private Group grpScanningInfo;
	private Spinner numberOfCopiesSpn;
	private ProgressBar progressBar;
	private Button savePrintingSettingsBtn;
	private Button autoPrintingChk;
	private Button printDialogChk;
	private Label infoUpcLbl;
	private Label barcodeLbl;
	private Label infoSizeLbl;
	private Label infoColorLbl;
	private Label infoItemLbl;
	private Label infoSkuLabel;
	private Button infoPreticketedCbx;
	private Label poDetailLbl;
	private Label packingDetailLbl;
	private Composite colorsComposite;
	private Group grpPoDetails;
	private Group grpPackingDetails;
	private Composite summaryScrolledComposite;
	private ScrolledComposite scrolledComposite;
	private Button btnCompleted;
	
	/**
	 * @wbp.parser.constructor
	 */
	public POClient( Display display) {
		super(display, SWT.SHELL_TRIM);
		
		init();
	}
	public POClient( Shell shell, Display display, User user ){
		super(display, SWT.SHELL_TRIM);
		this.user = user;
		try{
			shell.dispose();
			
			init();
			
			this.open();
			this.layout();
			while (!this.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		}catch( Exception e ){
			e.printStackTrace();
		}
		
	}
	
	private void init(){
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginTop = 5;
		gridLayout.marginRight = 5;
		gridLayout.marginLeft = 5;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);
		
		TabFolder tabFolder = new TabFolder(this, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TabItem tbtmScan = new TabItem(tabFolder, SWT.NONE);
		tbtmScan.setText("scan");
		
		Composite scanComposite = new Composite(tabFolder, SWT.NONE);
		tbtmScan.setControl(scanComposite);
		GridLayout gl_bottomScanComposite = new GridLayout(2, false);
		gl_bottomScanComposite.marginBottom = 5;
		gl_bottomScanComposite.marginTop = 5;
		gl_bottomScanComposite.marginRight = 5;
		gl_bottomScanComposite.marginLeft = 5;
		scanComposite.setLayout(gl_bottomScanComposite);
		
		Composite leftScanComposite = new Composite(scanComposite, SWT.BORDER);
		GridLayout gl_leftScanComposite = new GridLayout(4, false);
		gl_leftScanComposite.marginTop = 2;
		gl_leftScanComposite.marginRight = 2;
		gl_leftScanComposite.marginLeft = 2;
		gl_leftScanComposite.marginBottom = 2;
		leftScanComposite.setLayout(gl_leftScanComposite);
		GridData gd_leftScanComposite = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 2);
		gd_leftScanComposite.widthHint = 300;
		leftScanComposite.setLayoutData(gd_leftScanComposite);
		
		Label lblPo = new Label(leftScanComposite, SWT.NONE);
		lblPo.setAlignment(SWT.RIGHT);
		lblPo.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		lblPo.setText("po:");
		
		purchaseOrderCbx = new Combo(leftScanComposite, SWT.READ_ONLY);
		purchaseOrderCbx.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		purchaseOrderCbx.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		purchaseOrderCbx.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					getClient().selectingPurchaseOrder();
				} catch (JRException e) {
					getLog().error( "error on purchase order cbx selection", e );
					getClient().getErrorBox( e.getLocalizedMessage() );
				}
			}
		});
		
		Label itemLbl = new Label(leftScanComposite, SWT.NONE);
		itemLbl.setAlignment(SWT.RIGHT);
		itemLbl.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		itemLbl.setText("item:");
		
		itemCbx = new Combo(leftScanComposite, SWT.READ_ONLY);
		itemCbx.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		itemCbx.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					getClient().selectingFilter();
				} catch (JRException e1) {
					getLog().error( "error on selecting item filter", e1 );
					getClient().getErrorBox( e1.getMessage() );
				}
			}
		});
		itemCbx.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		Label colorLbl = new Label(leftScanComposite, SWT.NONE);
		colorLbl.setAlignment(SWT.RIGHT);
		colorLbl.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		colorLbl.setText("color:");
		
		colorCbx = new Combo(leftScanComposite, SWT.READ_ONLY);
		colorCbx.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		colorCbx.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					getClient().selectingFilter();
				} catch (JRException e1) {
					getLog().error( "error on selecting color filter", e1 );
					getClient().getErrorBox( e1.getMessage() );
				}
			}
		});
		colorCbx.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label sizeLbl = new Label(leftScanComposite, SWT.NONE);
		sizeLbl.setAlignment(SWT.RIGHT);
		sizeLbl.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		sizeLbl.setText("size:");
		
		sizeCbx = new Combo(leftScanComposite, SWT.READ_ONLY);
		sizeCbx.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		sizeCbx.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					getClient().selectingFilter();
				} catch (JRException e1) {
					getLog().error(  "error on selecting size filter", e1 );
					getClient().getErrorBox( e1.getMessage() );
				}
			}
		});
		sizeCbx.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		Label horizontalLine2 = new Label(leftScanComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		horizontalLine2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		poDetailLbl = new Label(leftScanComposite, SWT.NONE);
		poDetailLbl.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		poDetailLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		poDetailLbl.setText("po detail:");
		new Label(leftScanComposite, SWT.NONE);
		
		poDetailTbl = new Table(leftScanComposite, SWT.BORDER | SWT.FULL_SELECTION);
		poDetailTbl.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		poDetailTbl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					getClient().selectingPurchaseOrderDetail();
				} catch (JRException e1) {
					getLog().error( "error on selecting a purchase order detail", e1 );
					getClient().getErrorBox( e1.getMessage() );
				}
			}
		});
		GridData gd_poDetailTbl = new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1);
		gd_poDetailTbl.heightHint = 100;
		poDetailTbl.setLayoutData(gd_poDetailTbl);
		poDetailTbl.setHeaderVisible(true);
		poDetailTbl.setLinesVisible(true);
		
		TableColumn tableColumn = new TableColumn(poDetailTbl, SWT.NONE);
		tableColumn.setWidth(30);
		
		TableColumn tblclmnUpc = new TableColumn(poDetailTbl, SWT.NONE);
		tblclmnUpc.setWidth(105);
		tblclmnUpc.setText("upc");
		
		TableColumn tblclmnQty = new TableColumn(poDetailTbl, SWT.NONE);
		tblclmnQty.setWidth(30);
		tblclmnQty.setText("qty");
		
		TableColumn tblclmnCarton = new TableColumn(poDetailTbl, SWT.NONE);
		tblclmnCarton.setWidth(50);
		tblclmnCarton.setText("carton");
		
		TableColumn tblclmnReady = new TableColumn(poDetailTbl, SWT.NONE);
		tblclmnReady.setWidth(45);
		tblclmnReady.setText("ready");
		
		packingDetailLbl = new Label(leftScanComposite, SWT.NONE);
		packingDetailLbl.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		packingDetailLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		packingDetailLbl.setText("packing detail:");
		
		Button btnAddCarton = new Button(leftScanComposite, SWT.NONE);
		btnAddCarton.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		btnAddCarton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getClient().addCarton();
			}
		});
		btnAddCarton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnAddCarton.setText("add carton");
		
		packingDetailTbl = new Table(leftScanComposite, SWT.BORDER | SWT.FULL_SELECTION);
		packingDetailTbl.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		packingDetailTbl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					getClient().selectingPackingDetail();
				} catch (JRException e1) {
					getLog().error( "error on selecting packing detail from table", e1 );
					getClient().getErrorBox( e1.getMessage() );
				}
			}
		});
		GridData gd_packingDetailTbl = new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1);
		gd_packingDetailTbl.heightHint = 120;
		packingDetailTbl.setLayoutData(gd_packingDetailTbl);
		packingDetailTbl.setHeaderVisible(true);
		packingDetailTbl.setLinesVisible(true);
		
		TableColumn tblclmnSize = new TableColumn(packingDetailTbl, SWT.NONE);
		tblclmnSize.setWidth(44);
		tblclmnSize.setText("size");
		
		TableColumn tblclmnColor = new TableColumn(packingDetailTbl, SWT.NONE);
		tblclmnColor.setWidth(95);
		tblclmnColor.setText("color");
		
		TableColumn tblclmnSku = new TableColumn(packingDetailTbl, SWT.NONE);
		tblclmnSku.setWidth(36);
		tblclmnSku.setText("sku");
		
		TableColumn tblclmnQty_1 = new TableColumn(packingDetailTbl, SWT.NONE);
		tblclmnQty_1.setWidth(30);
		tblclmnQty_1.setText("qty");
		
		TableColumn tblclmnScanned = new TableColumn(packingDetailTbl, SWT.NONE);
		tblclmnScanned.setWidth(60);
		tblclmnScanned.setText("scanned");
		
		Group grpSumary = new Group(leftScanComposite, SWT.NONE);
		grpSumary.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.NORMAL));
		GridLayout gl_grpSumary = new GridLayout(1, false);
		gl_grpSumary.verticalSpacing = 0;
		grpSumary.setLayout(gl_grpSumary);
		grpSumary.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 4, 1));
		grpSumary.setText("summary");
		
		scrolledComposite = new ScrolledComposite(grpSumary, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		
		summaryScrolledComposite = new Composite(scrolledComposite, SWT.NONE);
		scrolledComposite.setContent(summaryScrolledComposite);
		scrolledComposite.setMinSize(summaryScrolledComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		summaryScrolledComposite.setLayout(new GridLayout(3, false));
		
		new Label(summaryScrolledComposite, SWT.NONE);
		//grpPoDetails = new Group(getMaster().getMaster().getHiddenShell(), SWT.NONE);
		grpPoDetails = new Group(summaryScrolledComposite, SWT.NONE);
		grpPoDetails.setFont(SWTResourceManager.getFont("Segoe UI", 7, SWT.NORMAL));
		GridLayout gl_grpPoDetails = new GridLayout(2, true);
		gl_grpPoDetails.marginTop = 2;
		grpPoDetails.setLayout(gl_grpPoDetails);
		GridData gd_grpPoDetails = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 2);
		gd_grpPoDetails.widthHint = 75;
		grpPoDetails.setLayoutData(gd_grpPoDetails);
		grpPoDetails.setText("PO Details");
		
		grpPackingDetails = new Group(summaryScrolledComposite, SWT.NONE);
		grpPackingDetails.setFont(SWTResourceManager.getFont("Segoe UI", 7, SWT.NORMAL));
		GridLayout gl_grpPackingDetails = new GridLayout(2, true);
		gl_grpPackingDetails.marginTop = 5;
		grpPackingDetails.setLayout(gl_grpPackingDetails);
		GridData gd_grpPackingDetails = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 2);
		gd_grpPackingDetails.widthHint = 75;
		grpPackingDetails.setLayoutData(gd_grpPackingDetails);
		grpPackingDetails.setText("Pk Details");
		
		colorsComposite = new Composite(summaryScrolledComposite, SWT.NONE);
		colorsComposite.setLayout(new GridLayout(1, false));
		colorsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite rightScanComposite = new Composite(scanComposite, SWT.BORDER);
		rightScanComposite.setLayout(new GridLayout(2, false));
		rightScanComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite topScanningComposite = new Composite(rightScanComposite, SWT.NONE);
		topScanningComposite.setLayout(new FormLayout());
		topScanningComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		
		Label lblNowScanning = new Label(topScanningComposite, SWT.NONE);
		FormData fd_lblNowScanning = new FormData();
		fd_lblNowScanning.bottom = new FormAttachment(100);
		fd_lblNowScanning.left = new FormAttachment(0, 29);
		lblNowScanning.setLayoutData(fd_lblNowScanning);
		lblNowScanning.setText("now scanning:");
		
		scannedBarTxt = new Text(topScanningComposite, SWT.BORDER);
		scannedBarTxt.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent event) {
				try {
					getClient().scanning();
				} catch (JRException e) {
					getLog().error( "error on scanning", e );
					getClient().getErrorBox( e.getMessage() );
				} catch (Exception e) {
					getLog().error( "error on scanning", e );
					getClient().getErrorBox( e.getMessage() );
				}
			}
		});
		FormData fd_scannedBarTxt = new FormData();
		fd_scannedBarTxt.top = new FormAttachment(100, -23);
		fd_scannedBarTxt.bottom = new FormAttachment(100);
		fd_scannedBarTxt.right = new FormAttachment(lblNowScanning, 167, SWT.RIGHT);
		fd_scannedBarTxt.left = new FormAttachment(lblNowScanning, 5);
		scannedBarTxt.setLayoutData(fd_scannedBarTxt);
		
		Composite scanningArea = new Composite(rightScanComposite, SWT.BORDER);
		scanningArea.setLayout(new GridLayout(1, false));
		GridData gd_scanningArea = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
		gd_scanningArea.widthHint = 318;
		scanningArea.setLayoutData(gd_scanningArea);
		
		scannedItemsTbl = new Table(scanningArea, SWT.BORDER | SWT.FULL_SELECTION);
		scannedItemsTbl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getClient().selectingScanDetail();
			}
		});
		scannedItemsTbl.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		scannedItemsTbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		scannedItemsTbl.setHeaderVisible(true);
		scannedItemsTbl.setLinesVisible(true);
		
		TableColumn tblclmnItems = new TableColumn(scannedItemsTbl, SWT.NONE);
		tblclmnItems.setWidth(45);
		tblclmnItems.setText("items");
		
		TableColumn tblclmnUpc_1 = new TableColumn(scannedItemsTbl, SWT.NONE);
		tblclmnUpc_1.setWidth(117);
		tblclmnUpc_1.setText("upc");
		
		TableColumn tblclmnSize_1 = new TableColumn(scannedItemsTbl, SWT.NONE);
		tblclmnSize_1.setWidth(57);
		tblclmnSize_1.setText("size");
		
		TableColumn tblclmnColor_1 = new TableColumn(scannedItemsTbl, SWT.NONE);
		tblclmnColor_1.setWidth(84);
		tblclmnColor_1.setText("color");
		
		Composite composite_1 = new Composite(scanningArea, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		composite_1.setLayout(new GridLayout(4, false));
		
		Button delBtn = new Button(composite_1, SWT.NONE);
		delBtn.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		delBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					getClient().deleteScan();
				} catch (Exception e1) {
					getLog().error( "error deleting the selected scan", e1);
				}
			}
		});
		delBtn.setText("del scan");
		
		Button cleanCartonBtn = new Button(composite_1, SWT.NONE);
		cleanCartonBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					getClient().cleanCarton();
				} catch (Exception e1) {
					getLog().error( "error cleaning the selected carton", e1 );
				}
			}
		});
		cleanCartonBtn.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		cleanCartonBtn.setText("clean carton");
		
		Button delCartonBtn = new Button(composite_1, SWT.NONE);
		delCartonBtn.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		delCartonBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					getClient().deleteCarton();
				} catch (Exception e1) {
					getLog().error( "error deleting the carton", e1 );
					getClient().getErrorBox( e1.getMessage() );
				}
			}
		});
		delCartonBtn.setText("del carton");
		
		btnCompleted = new Button(composite_1, SWT.TOGGLE);
		btnCompleted.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					getClient().completeCarton();
				} catch (Exception e1) {
					getLog().error( "error on completing the carton", e1 );
					getClient().getErrorBox(e1.getMessage());
				}
			}
		});
		btnCompleted.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		btnCompleted.setText("completed");
		
		Composite scanningTicketViewer = new Composite(rightScanComposite, SWT.BORDER);
		scanningTicketViewer.setLayout(new GridLayout(1, false));
		scanningTicketViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		grpTicketsComposite = new Group(scanningTicketViewer, SWT.NONE);
		grpTicketsComposite.setText("tickets");
		grpTicketsComposite.setLayout(new GridLayout(2, false));
		grpTicketsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite bottomScanComposite = new Composite(scanComposite, SWT.NONE);
		bottomScanComposite.setLayout(new GridLayout(2, false));
		GridData gd_bottomScanComposite = new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1);
		gd_bottomScanComposite.heightHint = 150;
		bottomScanComposite.setLayoutData( gd_bottomScanComposite );
		
		grpScanningInfo = new Group(bottomScanComposite, SWT.NONE);
		grpScanningInfo.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		grpScanningInfo.setLayout(new GridLayout(4, false));
		grpScanningInfo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpScanningInfo.setText("scanning info");
		
		infoUpcLbl = new Label(grpScanningInfo, SWT.NONE);
		infoUpcLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		infoUpcLbl.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		infoUpcLbl.setText("upc:");
		
		Label verticalLineLbl = new Label(grpScanningInfo, SWT.SEPARATOR | SWT.VERTICAL);
		verticalLineLbl.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 5));
		
		Label lblSize = new Label(grpScanningInfo, SWT.NONE);
		lblSize.setAlignment(SWT.RIGHT);
		lblSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblSize.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		lblSize.setText("size:");
		
		infoSizeLbl = new Label(grpScanningInfo, SWT.NONE);
		infoSizeLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		infoSizeLbl.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		infoSizeLbl.setText("");
		
		barcodeLbl = new Label( grpScanningInfo, SWT.IMAGE_PNG );
		barcodeLbl.setAlignment(SWT.CENTER);
		GridData gd_barcodeLbl = new GridData(SWT.CENTER, SWT.FILL, false, true, 1, 4);
		gd_barcodeLbl.widthHint = 150;
		barcodeLbl.setLayoutData(gd_barcodeLbl);
		
		Label lblColor = new Label(grpScanningInfo, SWT.NONE);
		lblColor.setAlignment(SWT.RIGHT);
		lblColor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblColor.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		lblColor.setText("color:");
		
		infoColorLbl = new Label(grpScanningInfo, SWT.NONE);
		infoColorLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		infoColorLbl.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		infoColorLbl.setText("");
		
		Label lblItem = new Label(grpScanningInfo, SWT.NONE);
		lblItem.setAlignment(SWT.RIGHT);
		lblItem.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblItem.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		lblItem.setText("item:");
		
		infoItemLbl = new Label(grpScanningInfo, SWT.NONE);
		infoItemLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		infoItemLbl.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		infoItemLbl.setText("");
		
		Label lblSku = new Label(grpScanningInfo, SWT.NONE);
		lblSku.setAlignment(SWT.RIGHT);
		lblSku.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblSku.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		lblSku.setText("sku:");
		
		infoSkuLabel = new Label(grpScanningInfo, SWT.NONE);
		infoSkuLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		infoSkuLabel.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		infoSkuLabel.setText("");
		
		infoPreticketedCbx = new Button(grpScanningInfo, SWT.CHECK);
		infoPreticketedCbx.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		infoPreticketedCbx.setText("preticketed");
				
		progressBar = new ProgressBar(grpScanningInfo, SWT.NONE);
		GridData gd_progressBar = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
		gd_progressBar.heightHint = 5;
		progressBar.setLayoutData(gd_progressBar);
		
		Group grpPrintingOptions = new Group(bottomScanComposite, SWT.NONE);
		grpPrintingOptions.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		GridLayout gl_grpPrintingOptions = new GridLayout(2, false);
		gl_grpPrintingOptions.verticalSpacing = 2;
		grpPrintingOptions.setLayout(gl_grpPrintingOptions);
		GridData gd_grpPrintingOptions = new GridData(SWT.RIGHT, SWT.FILL, false, true, 1, 1);
		gd_grpPrintingOptions.widthHint = 250;
		grpPrintingOptions.setLayoutData(gd_grpPrintingOptions);
		grpPrintingOptions.setText("printing settings");
		
		Label lblPrinters = new Label(grpPrintingOptions, SWT.NONE);
		lblPrinters.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		lblPrinters.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPrinters.setText("printers:");
		
		printersCbx = new Combo(grpPrintingOptions, SWT.READ_ONLY);
		printersCbx.setFont(SWTResourceManager.getFont("Segoe UI", 7, SWT.NORMAL));
		GridData gd_printersCbx = new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1);
		gd_printersCbx.heightHint = 14;
		printersCbx.setLayoutData(gd_printersCbx);
		
		Label lblCopies = new Label(grpPrintingOptions, SWT.NONE);
		lblCopies.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		lblCopies.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCopies.setText("copies:");
		
		numberOfCopiesSpn = new Spinner(grpPrintingOptions, SWT.BORDER);
		numberOfCopiesSpn.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		GridData gd_numberOfCopiesSpn = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_numberOfCopiesSpn.heightHint = 14;
		numberOfCopiesSpn.setLayoutData(gd_numberOfCopiesSpn);
		Label autoPrintingLbl = new Label(grpPrintingOptions, SWT.NONE);
		autoPrintingLbl.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		autoPrintingLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		autoPrintingLbl.setText( "auto:" );
		
		autoPrintingChk = new Button(grpPrintingOptions, SWT.CHECK);
		autoPrintingChk.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		autoPrintingChk.setToolTipText("automatic printing after completing the carton");
		
		Label lblPrintdialog = new Label(grpPrintingOptions, SWT.NONE);
		lblPrintdialog.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		lblPrintdialog.setText("print-dialog:");
		
		printDialogChk = new Button(grpPrintingOptions, SWT.CHECK);
		printDialogChk.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		new Label(grpPrintingOptions, SWT.NONE);
		
		savePrintingSettingsBtn = new Button(grpPrintingOptions, SWT.NONE);
		savePrintingSettingsBtn.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		savePrintingSettingsBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String defaultPrinter = getPrintersCbx().getText();
				int numberOfCopies = Integer.parseInt( getNumberOfCopiesSpn().getText() );
				boolean automaticPrinting = getAutoPrintingChk().getSelection();
				
				try {
					getMaster().setDefaultTicketPrinter( defaultPrinter );
					getMaster().setNumberOfCopies( numberOfCopies );
					getMaster().setAutomaticPrinting( automaticPrinting );
					
					if( getMaster().saveMasterProperties() ){
						getClient().getInformationBox( "printing settings saved:\n - default-printer: "+defaultPrinter+"\n - number-of-copies: " + numberOfCopies );
					}else{
						getClient().getErrorBox( "there is a problem saving printing settings, please contact your sysadmin" );
					}
				} catch (IOException e1) {
					getLog().info( "error saving printing settings", e1 );
				}
				
			}
		});
		savePrintingSettingsBtn.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, false, 1, 1));
		savePrintingSettingsBtn.setText("save");
		
		Composite composite = new Composite(this, SWT.BORDER);
		GridData gd_composite = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_composite.heightHint = 30;
		composite.setLayoutData(gd_composite);
		
		getPolybagViewer();
		getTicketViewer();
		
		createContents();
	}
	protected void createContents() {
		setText("purchase order client");
		setSize(1037, 748);
		
		try {
			PrintingUtils printing = new PrintingUtils(getMaster(), getShell());
			int printerIndex = -1;
			int i = 0;
			for( PrintService print : printing.getServices() ){
				if( getMaster().getDefaultTicketPrinter()!=null && !getMaster().getDefaultTicketPrinter().isEmpty() ){
					if( getMaster().getDefaultTicketPrinter().trim().compareTo( print.getName().trim() )==0 ){
						printerIndex = i;
					}
					//getLog().info( "default-ticket-printer:" + getMaster().getDefaultTicketPrinter() + ", print: " + print.getName() + ", printerIndex: " + printerIndex );
				}
				i++;
				getPrintersCbx().add( print.getName() );
				getPrintersCbx().setData( print.getName(), print);
			}
			getPrintersCbx().select(printerIndex);
			getNumberOfCopiesSpn().setSelection(getMaster().getNumberOfCopies());
			getAutoPrintingChk().setSelection( getMaster().isAutomaticPrinting() );
			getPrintDialogChk().setSelection( getMaster().isPrintingDialogEnabled() );
		} catch (IOException e) {
			getLog().error("error", e);
			getClient().getErrorBox( e.getMessage() );
		}
		
		getClient().loadAvailablePurchaseOrders();
	}
	private Logger getLog(){
		if( log == null ){
			log = Logger.getLogger(POClient.class);
			PropertyConfigurator.configure( "log4j.properties" );
		}
		return log;
	}
	@Override
	protected void checkSubclass() {
	}
	public Combo getPurchaseOrderCbx() {
		return purchaseOrderCbx;
	}
	public Combo getSizeCbx() {
		return sizeCbx;
	}
	public Combo getColorCbx() {
		return colorCbx;
	}
	public Combo getItemCbx() {
		return itemCbx;
	}
	public Table getPoDetailTbl() {
		return poDetailTbl;
	}
	public Table getPackingDetailTbl() {
		return packingDetailTbl;
	}
	
	public Text getScannedBarTxt() {
		return scannedBarTxt;
	}

	public void printCartonTicket(Carton carton) {
		getLog().info( "printing carton with upc: " + carton.getUpcCode() );
		try {
			PrintService service = null;
			try{
				service = (PrintService)getPrintersCbx().getData( getPrintersCbx().getItem( getPrintersCbx().getSelectionIndex() ) );
			}catch( Exception e ){
				getLog().error( "printCartonTicket, carton: " + carton.getId(), e );
				getClient().getErrorBox( e.getMessage() );
			}
			
			if( service!=null ) {
				PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
				MediaSizeName mediaSizeName = MediaSize.findMedia( 4, 4, MediaPrintableArea.INCH );
			    printRequestAttributeSet.add( mediaSizeName );
				printRequestAttributeSet.add( new Copies( 1 ) );
				JRPrintServiceExporter exporter = new JRPrintServiceExporter();
				exporter.setParameter( JRExporterParameter.JASPER_PRINT, getJasperPrintTicket( carton.getPackingDetail() ) );
				exporter.setParameter( JRPrintServiceExporterParameter.PRINT_SERVICE, service );
			    exporter.setParameter( JRPrintServiceExporterParameter.DISPLAY_PAGE_DIALOG, Boolean.FALSE );
			    exporter.setParameter( JRPrintServiceExporterParameter.DISPLAY_PRINT_DIALOG, getPrintDialogChk().getSelection() );
			    exporter.exportReport();
			} else {
				JasperPrintManager.printReport( getJasperPrintTicket( carton.getPackingDetail()), true );
			}
		} catch (JRException e) {
			getLog().error( "error printing the ticket", e );
			getClient().getErrorBox( e.getMessage() );
		}
	}
	
	public int getLastSku( PurchaseOrder po ){
		int lastSku = 0;
		
		List<PackingDetail> pdList = new ArrayList<PackingDetail>();
		
		for( PurchaseOrderDetail pod : po.getPurchaseOrderDetails() ){
			if( !pod.getDeleted() ){
				for( PackingDetail pd : pod.getPackingDetails() ){
					pdList.add( pd );
				}
			}
		}
		
		Collections.sort(pdList, new PackageDetailComparator() );
		lastSku = pdList.get(pdList.size()-1).getSku();
		
		return lastSku;
	}
	
	/*
	 * main method
	 */
	public static void main(String args[]) {
		try {
			Display display = Display.getDefault();
			POClient shell = new POClient(display);
			shell.open();
			shell.layout();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * scannin control methods
	 */
	public boolean isScanning(){
		boolean scanning = false;
		
		scanning = !(!getPoDetailTbl().getEnabled() && !getSizeCbx().getEnabled() && !getColorCbx().getEnabled() && 
				!getItemCbx().getEnabled() && !getPoDetailTbl().getEnabled() && !getPackingDetailTbl().getEnabled());
		
		return scanning;
	}
	public boolean lockForScanning(){
		boolean locked = false;
		
		getPoDetailTbl().setEnabled(false);
		getSizeCbx().setEnabled(false);
		getColorCbx().setEnabled(false);
		getItemCbx().setEnabled(false);
		getPoDetailTbl().setEnabled(false);
		getPackingDetailTbl().setEnabled(false);
		
		locked = !(!getPoDetailTbl().getEnabled() && !getSizeCbx().getEnabled() && !getColorCbx().getEnabled() && 
				!getItemCbx().getEnabled() && !getPoDetailTbl().getEnabled() && !getPackingDetailTbl().getEnabled());
		
		return locked;
	}
	public boolean unlockScanningFinished(){
		boolean unlocked = false;
		
		getPoDetailTbl().setEnabled(true);
		getSizeCbx().setEnabled(true);
		getColorCbx().setEnabled(true);
		getItemCbx().setEnabled(true);
		getPoDetailTbl().setEnabled(true);
		getPackingDetailTbl().setEnabled(true);
		getLayout();
		
		unlocked = (getPoDetailTbl().getEnabled() && getSizeCbx().getEnabled() && getColorCbx().getEnabled() && 
				getItemCbx().getEnabled() && getPoDetailTbl().getEnabled() && getPackingDetailTbl().getEnabled() );
		
		return unlocked;
	}
	public Table getScannedItemsTbl() {
		return scannedItemsTbl;
	}
	public int getPoDetailQty(){
		return poDetailQty;
	}
	public void setPoDetailQty( int poDetailQty ){
		this.poDetailQty = poDetailQty;
	}
	
	public Master getMaster() throws IOException{
		if( master == null ){
			master = new Master(this);
		}
		return master;
	}
	public Group getGrpTicketsComposite() {
		return grpTicketsComposite;
	}
	public Combo getPrintersCbx() {
		return printersCbx;
	}
	public Group getGrpScanningInfo() {
		return grpScanningInfo;
	}
	public Spinner getNumberOfCopiesSpn() {
		return numberOfCopiesSpn;
	}
	public ProgressBar getProgressBar() {
		return progressBar;
	}
	public Button getSavePrintingSettingsBtn() {
		return savePrintingSettingsBtn;
	}
	public Button getAutoPrintingChk() {
		return autoPrintingChk;
	}
	public Button getPrintDialogChk() {
		return printDialogChk;
	}
	public Label getInfoUpcLbl() {
		return infoUpcLbl;
	}
	public Label getBarcodeLbl() {
		return barcodeLbl;
	}
	public Label getInfoSizeLbl() {
		return infoSizeLbl;
	}
	public Label getInfoColorLbl() {
		return infoColorLbl;
	}
	public Label getInfoItemLbl() {
		return infoItemLbl;
	}
	public Label getInfoSkuLabel() {
		return infoSkuLabel;
	}
	public Button getInfoPreticketedCbx() {
		return infoPreticketedCbx;
	}
	public Label getPoDetailLbl() {
		return poDetailLbl;
	}
	public Label getPackingDetailLbl() {
		return packingDetailLbl;
	}
	public Composite getColorsComposite() {
		return colorsComposite;
	}
	public Group getGrpPoDetails() {
		return grpPoDetails;
	}
	public Group getGrpPackingDetails() {
		return grpPackingDetails;
	}
	public Composite getSummaryScrolledComposite() {
		return summaryScrolledComposite;
	}
	public ScrolledComposite getScrolledComposite() {
		return scrolledComposite;
	}
	public Button getBtnCompleted() {
		return btnCompleted;
	}
	public User getUser() {
		return user;
	}
	public ClientLogic getClient() {
		if( client == null ){
			client = new ClientLogic(this, getUser());
		}
		return client;
	}
}