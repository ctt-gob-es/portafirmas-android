package es.gob.afirma.android.signfolder.activities;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import es.gob.afirma.android.crypto.DnieConnectionManager;
import es.gob.afirma.android.fcm.NotificationUtilities;
import es.gob.afirma.android.gui.ConfirmSignatureDialog;
import es.gob.afirma.android.signfolder.BuildConfig;
import es.gob.afirma.android.signfolder.CryptoConfiguration;
import es.gob.afirma.android.signfolder.CustomAlertDialog;
import es.gob.afirma.android.signfolder.MessageDialog;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.SignfolderApp;
import es.gob.afirma.android.signfolder.listeners.DialogFragmentListener;
import es.gob.afirma.android.signfolder.listeners.OperationRequestListener;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.DocumentData;
import es.gob.afirma.android.signfolder.proxy.RequestDetail;
import es.gob.afirma.android.signfolder.proxy.RequestDocument;
import es.gob.afirma.android.signfolder.proxy.RequestResult;
import es.gob.afirma.android.signfolder.proxy.SignLine;
import es.gob.afirma.android.signfolder.proxy.SignLineElement;
import es.gob.afirma.android.signfolder.proxy.SignRequest;
import es.gob.afirma.android.signfolder.proxy.SignRequest.RequestType;
import es.gob.afirma.android.signfolder.proxy.SignRequestDocument;
import es.gob.afirma.android.signfolder.proxy.SignaturePermission;
import es.gob.afirma.android.signfolder.tasks.ApproveRequestsTask;
import es.gob.afirma.android.signfolder.tasks.CleanTempFilesTask;
import es.gob.afirma.android.signfolder.tasks.DownloadDocumentDataTask;
import es.gob.afirma.android.signfolder.tasks.DownloadFileTask;
import es.gob.afirma.android.signfolder.tasks.DownloadFileTask.DownloadDocumentListener;
import es.gob.afirma.android.signfolder.tasks.FireSignTask;
import es.gob.afirma.android.signfolder.tasks.LoadPetitionDetailsTask;
import es.gob.afirma.android.signfolder.tasks.LoadPetitionDetailsTask.LoadSignRequestDetailsListener;
import es.gob.afirma.android.signfolder.tasks.LogoutRequestTask;
import es.gob.afirma.android.signfolder.tasks.OpenHelpDocumentTask;
import es.gob.afirma.android.signfolder.tasks.RejectRequestsTask;
import es.gob.afirma.android.signfolder.tasks.SaveFileTask;
import es.gob.afirma.android.signfolder.tasks.VerifyRequestsTask;
import es.gob.afirma.android.user.configuration.ConfigurationConstants;
import es.gob.afirma.android.user.configuration.ConfigurationRole;
import es.gob.afirma.android.user.configuration.RoleInfo;
import es.gob.afirma.android.user.configuration.UserConfig;
import es.gob.afirma.android.util.AOUtil;
import es.gob.afirma.android.util.Base64;
import es.gob.afirma.android.util.PfLog;

/**
 * Actividad con el detalle de las peticiones.
 */
public final class PetitionDetailsActivity extends SignatureFragmentActivity implements LoadSignRequestDetailsListener,
        DownloadDocumentListener, DialogFragmentListener, ConfirmSignatureDialog.ConfirmSignatureDialogListener {

    /**
     * Identificador de la firma PAdES.
     */
    public static final String SIGN_FORMAT_PADES = "PAdES"; //$NON-NLS-1$
    /**
     * Identificador secundario de la firma PAdES.
     */
    public static final String SIGN_FORMAT_PDF = "PDF"; //$NON-NLS-1$
    /**
     * Identificador de la firma CAdES por defecto.
     */
    public static final String SIGN_FORMAT_CADES = "CAdES"; //$NON-NLS-1$
    /**
     * Tag para la presentaci&oacute;n de di&aacute;logos
     */
    protected final static String DIALOG_TAG = "dialog"; //$NON-NLS-1$

    static final String EXTRA_RESOURCE_DNI = "es.gob.afirma.signfolder.dni"; //$NON-NLS-1$
    static final String EXTRA_RESOURCE_CERT_ALIAS = "es.gob.afirma.signfolder.alias"; //$NON-NLS-1$
    static final String EXTRA_RESOURCE_REQUEST_ID = "es.gob.afirma.signfolder.requestId"; //$NON-NLS-1$
    static final String EXTRA_RESOURCE_REQUEST_STATE = "es.gob.afirma.signfolder.requestState"; //$NON-NLS-1$
    static final int RESULT_SIGN_OK = 1;
    static final int RESULT_REJECT_OK = 2;
    static final int RESULT_VERIFY_OK = 3;
    static final int RESULT_SESSION_FAILED = 5;
    static final int RESULT_SESSION_CLOSED = 6;
    static final int REQUEST_CODE = 3;
    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final String PDF_FILE_EXTENSION = ".pdf"; //$NON-NLS-1$
    private static final String DOC_FILE_EXTENSION = ".doc"; //$NON-NLS-1$
    private static final String DOCX_FILE_EXTENSION = ".docx"; //$NON-NLS-1$
    private static final String TXT_FILE_EXTENSION = ".txt"; //$NON-NLS-1$
    private static final String XML_FILE_EXTENSION = ".xml"; //$NON-NLS-1$

    private static final int PERMISSION_TO_DOWNLOAD_DOCUMENT = 1;
    private static final int PERMISSION_TO_DOWNLOAD_AND_SAVE_DOCUMENT = 2;
    private static final int PERMISSION_TO_DOWNLOAD_AND_SHARE_DOCUMENT = 3;
    private static final int PERMISSION_TO_DOWNLOAD_HELP = 4;

    private static final int PERMISSION_TO_NOTIFICATE = 5;

    /**
     * Di&aacute;logo para confirmar el cierre de la sesi&oacute;n.
     */
    private final static int DIALOG_CONFIRM_EXIT = 12;
    /**
     * Di&aacute;logo para confirmar la firma de peticiones.
     */
    private final static int DIALOG_CONFIRM_OPERATION = 15;
    /**
     * Di&aacute;logo para informar de un error.
     */
    private final static int DIALOG_MSG_ERROR = 16;
    /**
     * Di&aacute;logo para confirmar el rechazo de peticiones.
     */
    private final static int DIALOG_CONFIRM_REJECT = 13;
    private final static String PADES_EXTENSION = "pdf";  //$NON-NLS-1$
    private final static String CADES_EXTENSION = "csig";  //$NON-NLS-1$
    private final static String XADES_EXTENSION = "xsig";  //$NON-NLS-1$
    private String certAlias = null;
    private RequestDetail reqDetails = null;
    private String requestState = null;
    private List<File> tempDocuments = null;

    private boolean writePerm;

    /**
     * Attributo que representa el rol seleccionado durante la autenticación del usuario.
     */
    private ConfigurationRole selectedRole;
    private RoleInfo roleInfo;
    private ProgressDialog progressDialog = null;
    private TabHost th;
    private CustomAlertDialog dialog;
    private DocItem selectedDocItem = null;
    private String dni;
    private String certB64;
    private ArrayList<String> appIds;
    private ArrayList<String> appNames;
    private UserConfig userConfig;

    /**
     * Cadena con el listado de parametros para conceder o denegar los permisos necesarios
     * para completar las operaciones de firma de las peticiones que se estan procesando.
     */
    private String confirmationPermissionConfig;

    //metodo para crear la pestana del tab customizada
    private static View createTabView(final Context context, final String text) {
        final View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
        final TextView tv = view.findViewById(R.id.tabsText);
        tv.setText(text);
        return view;
    }

    /**
     * Devuelve la extensi&oacute;n correspondiente a una firma a partir del formato de firma utilizado.
     *
     * @param signFormat Formato de firma.
     */
    private static String getSignatureExtension(final String signFormat) {
        String ext;
        if (SIGN_FORMAT_PADES.equalsIgnoreCase(signFormat) || SIGN_FORMAT_PDF.equalsIgnoreCase(signFormat)) {
            ext = PADES_EXTENSION;
        } else if (SIGN_FORMAT_CADES.equalsIgnoreCase(signFormat)) {
            ext = CADES_EXTENSION;
        } else {
            ext = XADES_EXTENSION;
        }

        return ext;
    }

    ProgressDialog getProgressDialog() {
        return this.progressDialog;
    }

    void setProgressDialog(final ProgressDialog pd) {
        this.progressDialog = pd;
    }

    TabHost getTabHost() {
        return this.th;
    }

    void setTabHost(final TabHost th) {
        this.th = th;
    }

    CustomAlertDialog getCustomAlertDialog() {
        return this.dialog;
    }

    private void setCustomAlertDialog(final CustomAlertDialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // recuperamos el rol con el que se ha autenticado el usuario.
        this.roleInfo = (RoleInfo) getIntent().getSerializableExtra(ConfigurationConstants.EXTRA_RESOURCE_ROLE_SELECTED);
        if (roleInfo != null) {
            this.selectedRole = ConfigurationRole.getValue(roleInfo.getRoleId());
        }

        // Recuperamos aquellos parámetros necesarios para el cambio de rol.
        this.dni = getIntent().getStringExtra(PetitionListActivity.EXTRA_RESOURCE_DNI);
        this.certB64 = getIntent().getStringExtra(PetitionListActivity.EXTRA_RESOURCE_CERT_B64);
        this.appIds = getIntent().getStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_IDS);
        this.appNames = getIntent().getStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_NAMES);
        this.userConfig = (UserConfig) getIntent().getSerializableExtra(ConfigurationConstants.EXTRA_RESOURCE_USER_CONFIG);


        int layout;
        switch (getIntent().getStringExtra(EXTRA_RESOURCE_REQUEST_STATE)) {
            case SignRequest.STATE_UNRESOLVED:
                layout = R.layout.activity_unresolved_petition_details;
                break;
            case SignRequest.STATE_REJECTED:
                layout = R.layout.activity_rejected_petition_details;
                break;
            default:
                layout = R.layout.activity_resolved_petition_details;
        }
        setContentView(layout);

        setTabHost((TabHost) findViewById(android.R.id.tabhost));
        getTabHost().setup();
        getTabHost().getTabWidget().setDividerDrawable(R.drawable.tab_divider);

        setupTab(this.getResources().getString(R.string.details), 1);
        setupTab(this.getResources().getString(R.string.sign_lines), 2);
        setupTab(this.getResources().getString(R.string.docs), 3);
        getTabHost().setCurrentTab(0);

        // Comprobamos si tenemos permisos de escritura, por si tenemos que descargar ficheros.
        // En Android 11 y superiores consideraremos que ya los tenemos, ya que vamos a usar un
        // directorio publico
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            writePerm = true;
        }
        else {
            writePerm = (
                    ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
            );
        }
    }

    /**
     * Método para cargar el contenido de cada pesta&ntilde;a del panel de detalle.
     *
     * @param tag   Pesta&ntilde;a a cargar.
     * @param order Posici&oacute;n en la que colocarla.
     */
    private void setupTab(final String tag, final int order) {
        final View tabview = createTabView(getTabHost().getContext(), tag);
        if (order == 1) {
            getTabHost().addTab(getTabHost().newTabSpec(String.valueOf(R.id.tab1)).setIndicator(tabview).setContent(R.id.tab1));
        } else if (order == 2) {
            getTabHost().addTab(getTabHost().newTabSpec(String.valueOf(R.id.tab2)).setIndicator(tabview).setContent(R.id.tab2));
        } else if (order == 3) {
            getTabHost().addTab(getTabHost().newTabSpec(String.valueOf(R.id.tab3)).setIndicator(tabview).setContent(R.id.tab3));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        this.requestState = getIntent().getStringExtra(EXTRA_RESOURCE_REQUEST_STATE);
        if (getIntent() != null &&
                getIntent().getStringExtra(EXTRA_RESOURCE_REQUEST_ID) != null &&
                (getIntent().getStringExtra(EXTRA_RESOURCE_DNI) != null ||
                        getIntent().getStringExtra(EXTRA_RESOURCE_CERT_ALIAS) != null)) {
            this.certAlias = getIntent().getStringExtra(EXTRA_RESOURCE_CERT_ALIAS);

            if (this.reqDetails != null) {
                showRequestDetails(this.reqDetails);
            } else {
                final LoadPetitionDetailsTask lpdt = new LoadPetitionDetailsTask(
                        getIntent().getStringExtra(EXTRA_RESOURCE_REQUEST_ID),
                        this.roleInfo != null ? this.roleInfo.getOwnerDni() : this.dni,
                        CommManager.getInstance(),
                        this);
                showProgressDialog(getString(R.string.dialog_msg_loading), this, lpdt);
                lpdt.execute();
            }
        }
    }

    /**
     * Carga en las pesta&ntilde;as de los paneles de detalle de la solicitud la informaci&oacute;n
     * de la solicitud introducida.
     *
     * @param details Solicitud de la que mostrar los datos.
     */
    @Override
    public void loadedSignRequestDetails(final RequestDetail details) {

        dismissProgressDialog();

        this.reqDetails = details;

        if (this.selectedRole != null && this.selectedRole.equals(ConfigurationRole.VERIFIER)) {
            this.reqDetails.setType(RequestType.VERIFY);
        }

        showRequestDetails(details);
    }

    private void showRequestDetails(final RequestDetail details) {
        // Pestana de detalle
        ((TextView) findViewById(R.id.subjectValue)).setText(details.getSubject());
        ((TextView) findViewById(R.id.referenceValue)).setText(details.getRef());
        ((TextView) findViewById(R.id.dateValue)).setText(details.getDate());
        ((TextView) findViewById(R.id.expDateValue)).setText(
                details.getExpirationDate() != null ?
                        details.getExpirationDate() : getString(R.string.no_detail_data));
        ((TextView) findViewById(R.id.applicationValue)).setText(details.getApp());


        if (details.getMessage() != null) {
            TextView messageValue = (TextView) findViewById(R.id.messageValue);
            messageValue.setText(
                    details.getMessage() != null ?
                            linkifyHtml( details.getMessage().replaceAll("\r\n", "<br>") , Linkify.ALL) : ""
            );
            messageValue.setMovementMethod(LinkMovementMethod.getInstance());
        }

        if (findViewById(R.id.rejectReasonValue) != null) {
            ((TextView) findViewById(R.id.rejectReasonValue)).setText(
                    details.getRejectReason() != null ?
                            details.getRejectReason() : getString(R.string.no_detail_data)
            );
        }

        if (findViewById(R.id.rejectReasonValue) != null) {
            ((TextView) findViewById(R.id.rejectReasonValue)).setText(
                    details.getRejectReason() != null ?
                            details.getRejectReason() : getString(R.string.no_detail_data)
            );
        }

        //Dependiendo del tipo de peticion el boton tendra el texto de firmar o de visto bueno
        if (SignRequest.STATE_UNRESOLVED.equals(this.requestState)) {
            final Button typeButton = findViewById(R.id.btnSign);
            if (details.getType() == RequestType.SIGNATURE) {
                typeButton.setText(R.string.sign);
            } else if (details.getType() == RequestType.VERIFY) {
                typeButton.setText(R.string.verify);
                Button rejectBtn = findViewById(R.id.btnReject);
                rejectBtn.setEnabled(false);
                rejectBtn.setVisibility(View.GONE);
            } else {
                typeButton.setText(R.string.vb);
            }
        }

        // Listado de remitentes en la pestana de detalle
        final ListView sendersList = findViewById(R.id.listSenders);
        sendersList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, details.getSenders()));

        // Listado de documentos en la pestana de documentos
        final List<SignRequestDocument> documentsList = Arrays.asList(details.getDocs());
        final List<RequestDocument> attachedList = Arrays.asList(details.getAttached());
        final ListView docsList = findViewById(R.id.listDocs);
        docsList.setAdapter(new SignLineArrayAdapter(this, prepareDocsItems(documentsList, attachedList)));

        // Listado de lineas de firma en la pestana de lineas de firma
        final ListView signLinesList = findViewById(R.id.listSignLines);
        TextView tv = findViewById(R.id.titleSignLinesType);
        tv.setText(getString(R.string.signature_lines_header_list, details.getSignLinesType()));
        SignLineArrayAdapter signLines = new SignLineArrayAdapter(this, prepareSignLineItems(details.getSignLines()));
        signLinesList.setAdapter(signLines);
    }

    private static Spannable linkifyHtml(String html, int linkifyMask) {
        Spanned text = Html.fromHtml(html);
        URLSpan[] currentSpans = text.getSpans(0, text.length(), URLSpan.class);

        SpannableString buffer = new SpannableString(text);
        Linkify.addLinks(buffer, linkifyMask);

        for (URLSpan span : currentSpans) {
            int end = text.getSpanEnd(span);
            int start = text.getSpanStart(span);
            buffer.setSpan(span, start, end, 0);
        }
        return buffer;
    }

    private List<RequestDetailAdapterItem> prepareDocsItems(final List<SignRequestDocument> documentsList, final List<RequestDocument> attachedList) {
        final List<RequestDetailAdapterItem> list = new ArrayList<>();
        list.add(new DocsHeader(getString(R.string.docs)));
        for (final SignRequestDocument doc : documentsList) {
            list.add(new DocItem(doc.getName(), doc.getSize(), doc.getMimeType(),
                    doc.getId(), DownloadFileTask.DOCUMENT_TYPE_DATA));
        }

        // Anexos
        if (attachedList.size() > 0) {
            list.add(new DocsHeader(getString(R.string.attached)));
            for (final RequestDocument doc : attachedList) {
                list.add(new DocItem(doc.getName(), doc.getSize(), doc.getMimeType(),
                        doc.getId(), DownloadFileTask.DOCUMENT_TYPE_DATA));
            }
        }
        if (SignRequest.STATE_SIGNED.equals(this.requestState) && this.reqDetails.getType() == RequestType.SIGNATURE) {

            // Firmas
            list.add(new DocsHeader(getString(R.string.signs)));
            for (int i = 0; i < documentsList.size(); i++) {
                list.add(new DocItem(
                        getString(R.string.sign_filename, documentsList.get(i).getName(),
                                getSignatureExtension(documentsList.get(i).getSignFormat())),
                        documentsList.get(i).getId(), DownloadFileTask.DOCUMENT_TYPE_SIGN));
            }

            // Informes de firma
            list.add(new DocsHeader(getString(R.string.sign_reports)));
            for (int i = 0; i < documentsList.size(); i++) {
                list.add(new DocItem(
                        getString(R.string.sign_report, documentsList.get(i).getName()),
                        documentsList.get(i).getId(), DownloadFileTask.DOCUMENT_TYPE_REPORT));
            }
        }
        return list;
    }

    private List<RequestDetailAdapterItem> prepareSignLineItems(final Vector<SignLine> signLines) {
        final List<RequestDetailAdapterItem> list = new ArrayList<>();
        for (int i = 0; i < signLines.size(); i++) {
            final SignLine signLine = signLines.get(i);
            list.add(new SignLineHeader(getString(R.string.signline_header, i + 1), signLine));
            for (final SignLineElement signer : signLine.getSigners()) {
                list.add(new SignLineItem(signer));
            }
        }

        return list;
    }

    @Override
    public void errorLoadingSignRequestDetails() {
        dismissProgressDialog();
        Toast.makeText(this, getString(R.string.error_msg_loading_request_details), Toast.LENGTH_SHORT).show();
        // Ha ocurrido un error al cargar el detalle de la aplicacion, cerramos la actividad
        closeActivity();
    }


    @Override
    public void lostSession() {
        setResult(RESULT_SESSION_FAILED);
        this.closeActivity();
    }

    /**
     * Muestra un mensaje solicitando confirmacion al usuario para descargar y previsualizar
     * el fichero seleccionado.
    */
    private void showConfirmPreviewDialog() {

        if (selectedDocItem == null) {
            return;
        }

        // Si tenemos permisos de escritura, procedemos a la descarga. Si no, los pedimos

        if (writePerm) {
            downloadDocument(selectedDocItem.docId, selectedDocItem.name, selectedDocItem.mimetype, selectedDocItem.docType);
        } else {
            requestStoragePerm(PERMISSION_TO_DOWNLOAD_DOCUMENT);
        }
    }

    /**
     * Guarda un fichero en disco.
     */
    private void saveFile() {

        if (selectedDocItem == null) {
            return;
        }

        // Si tenemos permisos de escritura, procedemos a la descarga. Si no, los pedimos
        if (writePerm) {
            downloadAndSaveFile();
        } else {
            requestStoragePerm(PERMISSION_TO_DOWNLOAD_AND_SAVE_DOCUMENT);
        }
    }

    /**
     * Ejecuta el proceso necesario para compartir un archivo con otras aplicaciones
     */
    private void shareFile() {

        if (selectedDocItem == null) {
            return;
        }

        // Si tenemos permisos de escritura, procedemos a la descarga. Si no, los pedimos
        if (writePerm) {
            downloadAndShareFile();
        } else {
            requestStoragePerm(PERMISSION_TO_DOWNLOAD_AND_SHARE_DOCUMENT);
        }

    }

    private void downloadAndShareFile() {
        showProgressDialog(getString(R.string.loading_doc), this);
        DocumentData data = downloadDocData(selectedDocItem.docId, selectedDocItem.docType, selectedDocItem.name, selectedDocItem.mimetype);
        if (data != null && data.getDataIs() != null) {
            File file = null;
            try {
                byte[] fileData = AOUtil.getDataFromInputStream(data.getDataIs());
                String dataEncoded = Base64.encode(fileData);
                file = createAndSaveFileFromBase64Url("data:"+ data.getMimetype() + ";base64," + dataEncoded, true);
                Uri uri = FileProvider.getUriForFile(PetitionDetailsActivity.this,
                        BuildConfig.APPLICATION_ID + ".fileprovider", file);

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                if (selectedDocItem.mimetype != null) {
                    shareIntent.setType(selectedDocItem.mimetype);
                } else {
                    String mimeType = URLConnection.guessContentTypeFromName(file.getName());
                    shareIntent.setType(mimeType);
                }
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share)));
                if (this.tempDocuments == null) {
                    this.tempDocuments = new ArrayList<>();
                }
                this.tempDocuments.add(file);
            } catch (IOException ioe) {
                if (file != null) {
                    if (file.exists()) {
                        if (this.tempDocuments == null) {
                            this.tempDocuments = new ArrayList<>();
                        }
                        this.tempDocuments.add(file);
                    }
                }
                showDocumentError(R.string.toast_error_sharing);
            }
        }
        dismissProgressDialog();
    }

    /**
     * Ejecuta el proceso necesario para compartir un archivo con otras aplicaciones
     */
    private void downloadAndSaveFile() {

        if (selectedDocItem == null) {
            return;
        }

        DocumentData data = downloadDocData(selectedDocItem.docId, selectedDocItem.docType, selectedDocItem.name, selectedDocItem.mimetype);
        if (data != null && data.getDataIs() != null) {
            try {
                showProgressDialog(getString(R.string.loading_doc), this);
                byte[] fileData = AOUtil.getDataFromInputStream(data.getDataIs());
                String dataEncoded = Base64.encode(fileData);
                createAndSaveFileFromBase64Url("data:"+ data.getMimetype() + ";base64," + dataEncoded, false);
                dismissProgressDialog();
            } catch (IOException ioe) {
                showDocumentError(R.string.toast_error_saving);
            }
        }
    }

    private File downloadedFile = null;

    public File createAndSaveFileFromBase64Url(String url, boolean isShared) {
        File path;
        if (isShared) {
            path = SignfolderApp.getInternalTempDir();
        } else {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        }
        String filetype = "";
        if (selectedDocItem.docType == DownloadFileTask.DOCUMENT_TYPE_SIGN) {
            if (selectedDocItem.name != null && selectedDocItem.name.indexOf('.') != -1) {
                filetype = selectedDocItem.name.substring(selectedDocItem.name.lastIndexOf('.') + 1);
            }
        } else {
            filetype = url.substring(url.indexOf("/") + 1, url.indexOf(";"));
        }

        try {
            if(!path.exists()) {
                path.mkdirs();
            }

            String filename = selectedDocItem.name.substring(0, selectedDocItem.name.lastIndexOf(".")) +  "." + filetype;
            downloadedFile = new File(path, filename);

            int cont = 0;
            while (downloadedFile.exists()) {
                cont++;
                filename = selectedDocItem.name.substring(0, selectedDocItem.name.lastIndexOf(".")) + "("+ cont +")" + "." + filetype;
                downloadedFile = new File(path, filename);
            }
            downloadedFile.createNewFile();

            String base64EncodedString = url.substring(url.indexOf(",") + 1);
            byte[] decodedBytes = Base64.decode(base64EncodedString);
            OutputStream os = new FileOutputStream(downloadedFile);
            os.write(decodedBytes);
            os.close();

            MediaScannerConnection.scanFile(this,
                    new String[]{ downloadedFile.toString() }, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                 }
            });

            if (!isShared) {
                if (areNotificationsEnabled()) {
                    notifyDownloadAndSave();
                }
                else {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestNotificationPerm();
                    }
                    else {
                        Toast.makeText(this, getString(R.string.dialog_msg_saved_sign, downloadedFile.getName()), Toast.LENGTH_SHORT).show();
                    }
                }
            }

        } catch (IOException e) {
            downloadDocumentError();
            return null;
        }

        return downloadedFile;
    }

    /**
     * Metodo para crear y enviar notificacion al dispositivo sobre el archivo almacenado
     * @param file informacion sobre el archivo descargado
     */
    private void notifyDownloadAndSave() {

        File file = downloadedFile;

        // Preparamos el Intent para poder abrir el fichero desde la notificacion
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(PetitionDetailsActivity.this,
                BuildConfig.APPLICATION_ID + ".fileprovider", file);
        intent.setDataAndType(uri, selectedDocItem.mimetype);

        // Preparamos la notificacion
        Object resNotification = NotificationUtilities.createNotification(this, getString(R.string.notif_download_file_title),
                "El archivo " + file.getName() + " se ha descargado",
                R.drawable.ic_notification, Notification.DEFAULT_ALL, Notification.PRIORITY_HIGH,
                "PORTAFIRMAS", "DOWNLOAD_FILE",
                null, true,
                null, null);

        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) resNotification;
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE : 0));
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setAutoCancel(true);

        int notId = 888221;

        // Lanzamos la notificacion
        NotificationManager mNotifyMgr =
                (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(notId);
        mNotifyMgr.notify(notId, mBuilder.build());
    }

    private void downloadDocument(final String docId, final String filename, final String mimetype, final int docType) {

        // Para las firmas, avisaremos de que unicamente se van a descargar, mientras que el resto
        // de documentos se descargaran directamente para luego abrirse
        if (docType == DownloadFileTask.DOCUMENT_TYPE_SIGN) {
            final OnClickListener listener = new OnClickListener() {
                @Override
                public void onClick(final DialogInterface dlg, final int which) {
                    downloadAndSaveFile();
                }
            };
            final MessageDialog confirmPreviewDialog = new MessageDialog();
            confirmPreviewDialog.setMessage(getString(R.string.dialog_msg_confirm_save_sign));
            confirmPreviewDialog.setTitle(getString(R.string.dialog_msg_confirm_save_sign_title));
            confirmPreviewDialog.setListeners(listener, null);
            confirmPreviewDialog.setContext(this);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    confirmPreviewDialog.show(getSupportFragmentManager(), "ConfirmDialog"); //$NON-NLS-1$;
                }
            });
        } else {
            download(docId, docType, filename, mimetype, false);
        }
    }

    void download(final String docId, final int docType, final String filename, final String mimetype, boolean publicDir) {
        final DownloadFileTask dlfTask =
                new DownloadFileTask(docId, docType,
                        filename,
                        mimetype,
                        publicDir,
                        CommManager.getInstance(), this, getApplicationContext());
        showProgressDialog(getString(R.string.loading_doc), this, dlfTask);
        dlfTask.execute();
    }

    private DocumentData downloadDocData(final String docId, final int docType, final String filename, final String mimetype)  {
        final DownloadDocumentDataTask sfTask =
                new DownloadDocumentDataTask(docId, docType,
                        filename,
                        mimetype,
                        CommManager.getInstance(), this);
        AsyncTask result = sfTask.execute();
        try {
            return (DocumentData) result.get();
        } catch (Exception e) {
            showDocumentError(R.string.toast_error_donwloading);
            return null;
        }
    }

    private void requestStoragePerm(int actionId) {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                actionId
        );
    }

    private void requestNotificationPerm() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.POST_NOTIFICATIONS
                },
                PERMISSION_TO_NOTIFICATE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);



        if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                    this,
                    getString(R.string.nopermtopreviewdocs),
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        PfLog.i(SFConstants.LOG_TAG, "Se han concedido pemisos de acceso a disco");
        writePerm = true;

        switch (requestCode) {
            case PERMISSION_TO_DOWNLOAD_DOCUMENT:
                if (selectedDocItem == null) {
                    return;
                }
                downloadDocument(selectedDocItem.docId, selectedDocItem.name, selectedDocItem.mimetype, selectedDocItem.docType);
                break;
            case PERMISSION_TO_DOWNLOAD_AND_SAVE_DOCUMENT:
                if (selectedDocItem == null) {
                    return;
                }
                downloadAndSaveFile();
                break;

            case PERMISSION_TO_DOWNLOAD_AND_SHARE_DOCUMENT:
                if (selectedDocItem == null) {
                    return;
                }
                downloadAndShareFile();
                break;

            case PERMISSION_TO_DOWNLOAD_HELP:
                openHelp();
                break;

            case PERMISSION_TO_NOTIFICATE:
                notifyDownloadAndSave();
                break;
            default:
                PfLog.i(SFConstants.LOG_TAG, "Se ha pedido permisos de escritura en disco para una actividad desconocida");
                return;
        }
    }

    @Override
    public void downloadDocumentSuccess(final File documentFile, final String filename,
                                        final String mimetype, final int docType,
                                        final boolean externalDir) {
        dismissProgressDialog();

        // Si el fichero no es de firma, lo abrimos y guardamos una referencia al mismo para
        // borrarlo posteriormente
        if (docType != DownloadFileTask.DOCUMENT_TYPE_SIGN) {

            if (this.tempDocuments == null) {
                this.tempDocuments = new ArrayList<>();
            }
            this.tempDocuments.add(documentFile);

            openFile(documentFile, mimetype);
        } else {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PetitionDetailsActivity.this, R.string.toast_msg_download_ok, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (final Exception e) {
                PfLog.w(SFConstants.LOG_TAG, "No se pudo informar de que la firma se guardo correctamente", e); //$NON-NLS-1$
            }
        }
    }

    @Override
    public void downloadDocumentError() {
        dismissProgressDialog();

        final CustomAlertDialog dlg = CustomAlertDialog.newInstance(
                DIALOG_MSG_ERROR,
                getString(R.string.error),
                getString(R.string.toast_error_previewing),
                getString(android.R.string.ok),
                null,
                this
        );

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    dlg.show(getSupportFragmentManager(), DIALOG_TAG);
                }
                catch (Exception e) {
                    PfLog.w(SFConstants.LOG_TAG, "No se pudo mostrar el mensaje de error", e); //$NON-NLS-1$
                }
            }
        });
    }

    private void showDocumentError(int errorTextId) {
        dismissProgressDialog();

        final CustomAlertDialog dlg = CustomAlertDialog.newInstance(
                DIALOG_MSG_ERROR,
                getString(R.string.error),
                getString(errorTextId),
                getString(android.R.string.ok),
                null,
                this
        );

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    dlg.show(getSupportFragmentManager(), DIALOG_TAG);
                }
                catch (Exception e) {
                    PfLog.w(SFConstants.LOG_TAG, "No se pudo mostrar el mensaje de error", e); //$NON-NLS-1$
                }
            }
        });
    }

    /**
     * Abre y elimina un fichero
     *
     * @param documentFile Documento que se debe abrir.
     * @param mimetype     Tipo del documento.
     */
    private void openFile(final File documentFile, final String mimetype) {

        PfLog.i(SFConstants.LOG_TAG, "Abrimos el documento descargado con el MimeType: " + mimetype);

        Uri fileUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                documentFile);
        this.grantUriPermission(getPackageName(), fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(fileUri, mimetype);

        try {
            this.startActivity(intent);
        } catch (final ActivityNotFoundException e) {

            PfLog.w(SFConstants.LOG_TAG, "No se pudo abrir el fichero guardado. Se copiara al directorio de Descargas.", e); //$NON-NLS-1$

            // Copiamos el documento descargado al directorio de descargas (el fujo de datos se
            // cierra dentro de la tarea de copia)
            try {
                FileInputStream docFis = new FileInputStream(documentFile);
                SaveFileTask copyTask = new SaveFileTask(docFis, selectedDocItem.name, true, null);
                copyTask.execute();
            }
            catch (Exception e2) {
                PfLog.w(SFConstants.LOG_TAG, "No se pudo copiar el fichero descargado al directorio de externo", e2);
            }

            final MessageDialog md = new MessageDialog();
            md.setMessage(getString(R.string.error_file_not_support));
            md.setTitle(getString(R.string.error_title_openning_file));
            md.setContext(this);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        md.show(getSupportFragmentManager(), "ErrorDialog"); //$NON-NLS-1$
                    }
                    catch (Exception e) {
                        PfLog.w(SFConstants.LOG_TAG, "No se pudo mostrar el mensaje de error", e); //$NON-NLS-1$
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (this.tempDocuments != null) {
            for (final File doc : this.tempDocuments) {
                PfLog.i(SFConstants.LOG_TAG, "Se intenta borrar el fichero: " + doc.getAbsolutePath()); //$NON-NLS-1$
                doc.delete();
            }
        }
    }

    /**
     * Metodo que define la accion a realizar al pulsar en el boton Sign/Verify.
     *
     * @param v Vista desde la que se invoco el metodo
     */
    public void onClickSign(final View v) {
        if (this.reqDetails == null) {
            return;
        }

        final CustomAlertDialog dlg = CustomAlertDialog.newInstance(
                DIALOG_CONFIRM_OPERATION,
                getString(R.string.aviso),
                getConfirmDialogMessage(this.reqDetails.getType()),
                getString(android.R.string.ok),
                getString(R.string.cancel),
                this
        );

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    dlg.show(getSupportFragmentManager(), DIALOG_TAG);
                }
                catch (Exception e) {
                    PfLog.w(SFConstants.LOG_TAG, "No se pudo mostrar el mensaje de error", e); //$NON-NLS-1$
                }
            }
        });
    }

    //Metodo que devuelve un string indicando el tipo de operacion a realizar: firmar, vb
    private String getConfirmDialogMessage(final RequestType type) {
        if (type == RequestType.APPROVE) {
            return getString(R.string.dialog_msg_confirm_approve);
        } else if (this.selectedRole != null && this.selectedRole.equals(ConfigurationRole.VERIFIER)) {
            return getString(R.string.dialog_msg_confirm_verify);
        }
        return getString(R.string.dialog_msg_confirm_sign);
    }

    /**
     * Metodo que define la accion a realizar al pulsar en el boton Reject
     *
     * @param v Vista desde la que se invoco el metodo
     */
    public void onClickReject(final View v) {
        if (this.reqDetails == null) {
            return;
        }

        // Mostramos el dialogo de rechazo
        setCustomAlertDialog(CustomAlertDialog.newInstance(
                DIALOG_CONFIRM_REJECT,
                getString(R.string.dialog_title_confirm_reject),
                getString(R.string.dialog_msg_reject_request),
                getString(android.R.string.ok),
                getString(android.R.string.cancel),
                this)
        );

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getCustomAlertDialog().show(getSupportFragmentManager(), DIALOG_TAG);
            }
        });
    }

    private void processRequest(final RequestType type) {

        if (type == RequestType.SIGNATURE) {
            showProgressDialog(getString(R.string.dialog_msg_signing_1),  this);
            signRequest();
        } else if (type == RequestType.VERIFY) {
            showProgressDialog(getString(R.string.dialog_msg_verifying_1), this);
            verifyRequest();
        } else {
            showProgressDialog(getString(R.string.dialog_msg_approving_1), this);
            approveRequest();
        }
    }

    protected void signRequest() {
        signRequest(this.reqDetails);
    }

    protected void signRequest(SignRequest request) {
        // Reiniciamos la configuracion
        this.confirmationPermissionConfig = null;
        signRequests(new SignRequest[]{ request });
    }

    /**
     * Inicia el proceso de validación de petición.
     */
    private void verifyRequest() {
        VerifyRequestsTask verifyRequestTask = new VerifyRequestsTask(
                this.reqDetails.getId(), CommManager.getInstance(), this);
        verifyRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void enabledNfcCancelled() {
        dismissProgressDialog();
        Toast.makeText(getApplicationContext(), R.string.nfc_still_disabled, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Obtenemos la respuesta tras autorizar la operacion en Cl@ve Firma
        if (requestCode == LoadKeyStoreFragmentActivity.WEBVIEW_REQUEST_CODE) {
            // Si la peticion ha sido correcta iniciamos la finalizacion de firma
            if (resultCode == RESULT_OK) {

                PfLog.i(SFConstants.LOG_TAG, "Se han cargado correctamente los datos en FIRe");

                FireSignTask signTask = new FireSignTask(this);
                signTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else if (resultCode == RESULT_CANCELED) {
                PfLog.i(SFConstants.LOG_TAG, "Operacion de firma cancelada por el usuario");
                dismissProgressDialog();
            } else {
                PfLog.e(SFConstants.LOG_TAG, "Error al cargar los datos en FIRe"); //$NON-NLS-1$
                String errorType = data != null ? data.getStringExtra("type") : null; //$NON-NLS-1$

                PfLog.e(SFConstants.LOG_TAG, "Tipo de error: " + errorType);

                dismissProgressDialog();

                // TODO: Diferenciar segun tipo de error
                showToastMessage(getString(R.string.toast_msg_fire_comunication_ko));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void rejectRequest(final String reason) {
        new RejectRequestsTask(
                this.reqDetails.getId(), CommManager.getInstance(), this, reason).execute();
    }

    private void approveRequest() {
        new ApproveRequestsTask(
                this.reqDetails.getId(), CommManager.getInstance(), this).execute();
    }

    @Override
    public void requestOperationFinished(final int operation, final RequestResult requestResult) {

        dismissProgressDialog();

        if (requestResult.isStatusOk()) {
            int result;
            switch (operation) {
                case OperationRequestListener.REJECT_OPERATION:
                    result = PetitionDetailsActivity.RESULT_REJECT_OK;
                    break;
                case OperationRequestListener.VERIFY_OPERATION:
                    result = PetitionDetailsActivity.RESULT_VERIFY_OK;
                    break;
                default:
                    result = PetitionDetailsActivity.RESULT_SIGN_OK;
            }
            setResult(result);
            closeActivity();
        } else {
            requestOperationFailed(operation, requestResult, null);
        }
    }

    @Override
    public void requestOperationFailed(final int operation, final RequestResult requests, final Throwable t) {

        dismissProgressDialog();

        PfLog.e(SFConstants.LOG_TAG, "Ha fallado la operacion: " + t, t); //$NON-NLS-1$

        int msgId;
        switch (operation) {
            case OperationRequestListener.REJECT_OPERATION:
                msgId = R.string.error_msg_rejecting_request;
                break;
            case OperationRequestListener.VERIFY_OPERATION:
                msgId = R.string.error_msg_verifing_request;
                break;
            default:
                msgId = R.string.error_msg_procesing_request;
        }
        showToastMessage(getString(msgId));
    }

    @Override
    public void requestOperationCancelled(int operation) {
        dismissProgressDialog();

        PfLog.e(SFConstants.LOG_TAG, "El usuario ha cancelado la operacion"); //$NON-NLS-1$
    }

    @Override
    void requestedSignatureSuccess(SignRequest[] signRequests) {

        dismissProgressDialog();

        final RequestResult result = new RequestResult(signRequests[0].getId(), true);
        requestOperationFinished(OperationRequestListener.SIGN_OPERATION, result);
    }

    @Override
    void requestedSignatureFailed(SignRequest[] signRequests, Throwable cause) {

        dismissProgressDialog();

        final RequestResult result = new RequestResult(signRequests[0].getId(), false);
        requestOperationFailed(OperationRequestListener.SIGN_OPERATION, result, cause);
    }

    @Override
    public void requestOperationPendingToConfirm(SignRequest pendingRequest) {
        dismissProgressDialog();

        processPendingSignatures(pendingRequest.getPermissions().iterator());
    }

    /**
     * Procesa las peticiones pendientes, solicitando permisos si aun no se han concedido todos.
     * @param permissionsRequiredIt Iterador del listado de permisos.
     */
    private void processPendingSignatures(Iterator<SignaturePermission> permissionsRequiredIt) {
        // Comprobamos si quedan mas permisos sin pedir y lo hacemos en tal caso
        if (permissionsRequiredIt.hasNext()) {
            ConfirmSignatureDialog dialog = new ConfirmSignatureDialog();
            dialog.setPermissionIterator(permissionsRequiredIt);
            dialog.show(getFragmentManager(), "ConfirmSignatureDialog");
        }
        // Si ya hemos pedido todos los permisos, asignamos el valor que corresponde a cada uno
        // y volvemos a enviar las firmas
        else {
            // Incluimos los permisos en una copia de la peticion. No lo hacemos sobre la peticion
            // original por si hubiese problemas en la operacion, que en los siguientes intentos
            // se vuelva a pedir las confirmaciones
            SignRequest requestCopy = copyToSign(this.reqDetails);
            for (SignRequestDocument doc : requestCopy.getDocs()) {
                if (doc.getParams() == null || doc.getParams().trim().isEmpty()) {
                    doc.setParams(Base64.encode(this.confirmationPermissionConfig.getBytes()));
                } else {
                    doc.setParams(Base64.encode((doc.getParams() + "\n" + this.confirmationPermissionConfig).getBytes()));
                }
            }
            signRequest(requestCopy);
        }
    }

    private SignRequest copyToSign(SignRequest reqDetails) {

        SignRequestDocument[] docs = reqDetails.getDocs();
        SignRequestDocument[] newDocs = new SignRequestDocument[docs.length];
        for (int i = 0; i < docs.length; i++) {
            newDocs[i] = (SignRequestDocument) docs[i].clone();
        }
        return new SignRequest(reqDetails.getId(), reqDetails.getType(), newDocs);
    }


    /**
     * Muestra un mensaje en un toast.
     *
     * @param message Mensaje a mostrar.
     */
    void showToastMessage(final String message) {

        dismissProgressDialog();
        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(PetitionDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Cierra el activity liberando recursos.
     */
    void closeActivity() {
        this.reqDetails = null;
        finish();
    }

    @Override
    protected void showProgressDialog(final String message, final Context ctx, final AsyncTask<?, ?, ?>... tasks) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                ProgressDialog currentProgressDialog = getProgressDialog();
                if (currentProgressDialog != null && currentProgressDialog.isShowing()) {
                    currentProgressDialog.dismiss();
                }

                try {
                    currentProgressDialog = ProgressDialog.show(ctx, null, message, true);
                    setProgressDialog(currentProgressDialog);
                } catch (final Exception e) {
                    PfLog.e(SFConstants.LOG_TAG, "No se ha podido mostrar el dialogo de progreso: " + e, e); //$NON-NLS-1$
                    return;
                }

                // Definimos el comportamiento para cancelar los dialogos de espera
                currentProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(final DialogInterface dialog, final int keyCode, final KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            if (tasks != null) {
                                for (AsyncTask<?, ?, ?> task : tasks) {
                                    if (task != null) {
                                        task.cancel(true);
                                    }
                                }
                            }
                            dialog.dismiss();
                            return true;
                        }
                        return false;
                    }
                });
            }
        });
    }

    @Override
    protected void dismissProgressDialog() {
        if (getProgressDialog() != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getProgressDialog().dismiss();
                }
            });
        }
    }

    @Override
    public void onDialogPositiveClick(final int dialogId, final String reason) {

        // Dialogo de confirmacion de cierre de sesion
        if (dialogId == DIALOG_CONFIRM_EXIT) {
            CryptoConfiguration.setCertificateAlias(null);
            CryptoConfiguration.setCertificatePrivateKeyEntry(null);
            try {
                CleanTempFilesTask cleanTask = new CleanTempFilesTask(SignfolderApp.getInternalTempDir());
                cleanTask.execute();
            } catch (Exception e) {
                PfLog.e(SFConstants.LOG_TAG,
                        "No se ha podido ejecutar la tarea de borrado de temporales", e); //$NON-NLS-1$
            }

            // Eliminamos la informacion de conexion con el DNIe si la hubiese
            DnieConnectionManager.getInstance().clearCan();
            DnieConnectionManager.getInstance().reset();

            try {
                LogoutRequestTask lrt = new LogoutRequestTask(CommManager.getInstance());
                lrt.execute();
            } catch (Exception e) {
                PfLog.e(SFConstants.LOG_TAG,
                        "No se ha podido cerrar sesion: " + e); //$NON-NLS-1$
            }
            setResult(RESULT_SESSION_CLOSED);
            closeActivity();
        }
        // Dielogo de confirmacion de procesado de la peticion
        else if (dialogId == DIALOG_CONFIRM_OPERATION) {
            processRequest(this.reqDetails.getType());
        }
        // Dialogo de confirmacion de rechazo de peticiones
        else if (dialogId == DIALOG_CONFIRM_REJECT) {
            getCustomAlertDialog().dismiss();
            showProgressDialog(getString(R.string.dialog_msg_rejecting_1), this);
            rejectRequest(reason);
        }
    }

    @Override
    public void onDialogNegativeClick(final int dialogId) {
        // Si el usuario cancela el dialogo, se cierra y no hacemos nada
    }

    public void onConfirmSignatureDialogPositiveButton(SignaturePermission permission, Iterator<SignaturePermission> permissionsRequiredIt) {
        // Configuramos el permiso como concedido
        String permissionProperty = permission.getExtraParam() + "=true";
        if (this.confirmationPermissionConfig == null) {
            this.confirmationPermissionConfig = permissionProperty;
        } else {
            this.confirmationPermissionConfig += "\n" + permissionProperty;
        }

        processPendingSignatures(permissionsRequiredIt);
    }
    public void onConfirmSignatureDialogNegativeButton(SignaturePermission permission, Iterator<SignaturePermission> permissionsRequiredIt) {
        // Configuramos el permiso como concedido
        String permissionProperty = permission.getExtraParam() + "=false";
        if (this.confirmationPermissionConfig == null) {
            this.confirmationPermissionConfig = permissionProperty;
        } else {
            this.confirmationPermissionConfig += "\n" + permissionProperty;
        }

        processPendingSignatures(permissionsRequiredIt);
    }

    // Definimos el menu de opciones de la aplicacion, cuyas opciones estan
    // definidas
    // para cada listado de peticiones
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater()
                .inflate(
                        R.menu.activity_petition_details_options_menu,
                        menu);

        // Deshabilitamos la opción de acceder a la configuración de roles
        // si el rol con el que se ha accedido a la plataforma no es el de firmante.
//        if (this.selectedRole != null && menu.findItem(R.id.setting) != null) {
//            menu.findItem(R.id.setting).setEnabled(false);
//            menu.findItem(R.id.setting).setVisible(false);
//        }

        // Si solo existe el rol de firmante, no mostramos la opción de cambiar de rol.
        if (this.userConfig.getRoles().size() < 1) {
            menu.findItem(R.id.changeRole).setEnabled(false);
            menu.findItem(R.id.changeRole).setVisible(false);
        }

        return true;
    }

    // Definimos que hacer cuando se pulsa una opcion del menu de opciones de la
    // aplicacion
    // En el ejemplo se indica la opcion seleccionada
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Abrir ayuda
        if (item.getItemId() == R.id.help) {
            if (writePerm) {
                openHelp();
            } else {
                requestStoragePerm(PERMISSION_TO_DOWNLOAD_HELP);
            }

        } else if (item.getItemId() == R.id.changeRole) {
            Intent intent = new Intent(this, LoginWithRoleActivity.class);
            intent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_USER_CONFIG, this.userConfig);
            intent.putExtra(PetitionListActivity.EXTRA_RESOURCE_DNI, this.dni);
            intent.putExtra(PetitionListActivity.EXTRA_RESOURCE_CERT_B64, this.certB64);
            intent.putExtra(PetitionListActivity.EXTRA_RESOURCE_CERT_ALIAS, this.certAlias);
            intent.putStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_IDS, this.appIds);
            intent.putStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_NAMES, this.appNames);

            // Vaciamos la pila de actividades...
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // Iniciamos la nueva actividad.
            startActivity(intent);
            // Finalizamos la actividad actual.
            finish();
        }

        // Cerrar sesion
        else if (item.getItemId() == R.id.logout) {
            showConfirmExitDialog();
        }

        return true;
    }

    /**
     * Abre el fichero de ayuda de la aplicaci&oacute;n.
     */
    private void openHelp() {
        OpenHelpDocumentTask task = new OpenHelpDocumentTask(this);
        task.execute();
    }

    /**
     * Muestra un mensaje al usuario pidiendo confirmacion para cerrar la
     * sesi&oacute;n del usuario.
     */
    private void showConfirmExitDialog() {

        final CustomAlertDialog dialog = CustomAlertDialog.newInstance(
                DIALOG_CONFIRM_EXIT,
                getString(R.string.dialog_title_close_session),
                getString(R.string.dialog_msg_close_session),
                getString(android.R.string.ok),
                getString(android.R.string.cancel),
                this);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    dialog.show(getSupportFragmentManager(), DIALOG_TAG);
                }
                catch (Exception e) {
                    PfLog.w(SFConstants.LOG_TAG, "Error al mostrar la consulta de cierre de la sesion", e); //$NON-NLS-1$
                }
            }
        });
    }

    /**
     * Consulta si las notificaciones de la aplicacion estan habilitadas a nivel de sistema.
     * @return {@code true} cuando est&aacute;n habilitadas, {@code false} en caso contrario.
     */
    public boolean areNotificationsEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (!manager.areNotificationsEnabled()) {
                return false;
            }
            List<NotificationChannel> channels = manager.getNotificationChannels();
            for (NotificationChannel channel : channels) {
                if (channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                    return false;
                }
            }
            return true;
        } else {
            return NotificationManagerCompat.from(this).areNotificationsEnabled();
        }
    }

    /**
     * Tipo de elemento de la lista de lineas de firma (cabeceras y firmantes).
     */
    private enum SignLineItemType {
        SIGNLINE_ITEM, HEADER_ITEM, DOC_ITEM
    }


    /**
     * Interfaz que implementan los tipos de elemento que componen las l&iacute;neas de firma.
     */
    private interface RequestDetailAdapterItem {
        int getViewType();

        View getView(LayoutInflater inflater, View convertView);
    }

    class SignLineHeader implements RequestDetailAdapterItem {

        private final String name;
        private final SignLine signLine;

        SignLineHeader(final String name, final SignLine signLine) {
            this.name = name;
            this.signLine = signLine;
        }

        @Override
        public int getViewType() {
            return SignLineItemType.HEADER_ITEM.ordinal();
        }

        @Override
        public View getView(final LayoutInflater inflater, final View convertView) {
            View view;
            if (convertView == null) {
                view = inflater.inflate(R.layout.array_adapter_header_detail, null);
            } else {
                view = convertView;
            }
            final TextView text = view.findViewById(R.id.header);

            if ("VISTOBUENO".equals(signLine.getType())) {
                text.setText(getString(R.string.approves_header_list, this.name));
            } else {
                text.setText(getString(R.string.signatures_header_list, this.name));
            }

            // Lo marcamos como firmado si alguien de la lista ha firmado.
            if (checkIfSomeoneSigned(this.signLine.getSigners())) {
                ((ImageView) view.findViewById(R.id.sign_icon)).setImageResource(R.drawable.icon_signline_completed);
            } else {
                ((ImageView) view.findViewById(R.id.sign_icon)).setImageDrawable(null);
            }

            return view;
        }

        // Se comprueba si alguien ha firmado la linea de firma
        private boolean checkIfSomeoneSigned(final ArrayList<SignLineElement> signers) {
            for (final SignLineElement sle : signers) {
                if (sle.isDone()) {
                    return true;
                }
            }
            return false;
        }
    }

    class DocsHeader implements RequestDetailAdapterItem {

        private final String name;

        DocsHeader(final String name) {
            this.name = name;
        }

        @Override
        public int getViewType() {
            return SignLineItemType.HEADER_ITEM.ordinal();
        }

        @Override
        public View getView(final LayoutInflater inflater, final View convertView) {
            View view;
            if (convertView == null) {
                view = inflater.inflate(R.layout.array_adapter_header, null);
            } else {
                view = convertView;
            }
            final TextView text = view.findViewById(R.id.header);
            text.setText(this.name);

            return view;
        }
    }

    final class SignLineItem implements RequestDetailAdapterItem {
        private final SignLineElement signer;

        SignLineItem(final SignLineElement signer) {
            this.signer = signer;
        }

        @Override
        public int getViewType() {
            return SignLineItemType.SIGNLINE_ITEM.ordinal();
        }

        @Override
        public View getView(final LayoutInflater inflater, final View convertView) {
            View view;
            if (convertView == null) {
                view = inflater.inflate(R.layout.array_adapter_signline_item, null);
                // Do some initialization
            } else {
                view = convertView;
            }

            ((TextView) view.findViewById(R.id.signer)).setText(this.signer.getSigner());

            ((ImageView) view.findViewById(R.id.signerIcon)).setImageResource(R.drawable.icon_signline);

            ((TextView) view.findViewById(R.id.signer)).setTextColor(Color.BLACK);
            return view;
        }
    }

    final class DocItem implements RequestDetailAdapterItem, View.OnClickListener  {
        private final String docId;
        private final String name;
        private final String mimetype;
        private final int size;
        private final int docType;

        DocItem(final String name, final int size, final String mimetype, final String docId, final int docType) {
            this.name = name;
            this.size = size;
            this.mimetype = mimetype;
            this.docId = docId;
            this.docType = docType;
        }

        DocItem(final String name, final String docId, final int docType) {
            this.name = name;
            this.size = -1;
            this.mimetype = null;
            this.docId = docId;
            this.docType = docType;
        }

        @Override
        public int getViewType() {
            return SignLineItemType.DOC_ITEM.ordinal();
        }

        @Override
        public View getView(final LayoutInflater inflater, final View convertView) {
            View view;
            if (convertView == null) {
                view = inflater.inflate(R.layout.array_adapter_file_chooser, null);
            } else {
                view = convertView;
            }

            view.setBackgroundResource(R.drawable.array_adapter_selector_white);
            final ImageView icon = view.findViewById(R.id.fileIcon);
            final TextView t1 = view.findViewById(R.id.TextView01);
            final TextView t2 = view.findViewById(R.id.TextView02);

            if (this.name.endsWith(PDF_FILE_EXTENSION)) {
                icon.setImageResource(R.drawable.icon_pdf_file);
            } else if (this.name.endsWith(DOC_FILE_EXTENSION) || this.name.endsWith(DOCX_FILE_EXTENSION)) {
                icon.setImageResource(R.drawable.icon_doc_file);
            } else if (this.name.endsWith(TXT_FILE_EXTENSION)) {
                icon.setImageResource(R.drawable.icon_text_file);
            } else if (this.name.endsWith(XML_FILE_EXTENSION)) {
                icon.setImageResource(R.drawable.icon_xml_file);
            } else {
                icon.setImageResource(R.drawable.icon_file);
            }

            final TextView t3 = view.findViewById(R.id.textViewOptions);
            final DocItem docItemViewed = this;
            t3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PetitionDetailsActivity.this.selectedDocItem = docItemViewed;
                    PopupMenu popup = new PopupMenu(PetitionDetailsActivity.this, t3);
                    // Se mostraran los iconos a partir de Android 10 (API 29)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        popup.setForceShowIcon(true);
                    }
                    popup.getMenuInflater().inflate(R.menu.activity_document_options_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getTitle().equals(getString(R.string.download))) {
                                saveFile();
                            } else if (item.getTitle().equals(getString(R.string.share))) {
                                shareFile();
                            }
                            return true;
                        }
                    });

                    popup.show(); //showing popup menu
                }
            });

            t1.setText(this.name);
            if (this.size != -1) {
                t2.setText(getString(R.string.file_chooser_tamano_del_fichero, formatFileSize(this.size)));
            }

            view.setOnClickListener(this);

            return view;
        }

        /**
         * Devuelve una cadena con el tama&ntilde;o indicado  en bytes formateada.
         *
         * @param fileSize Tama&ntilde;o que se quiere representar.
         * @return Cadena que representa el tama&ntilde;o.
         */
        private String formatFileSize(final long fileSize) {

            if (fileSize < 1024) {
                return addDotMiles(fileSize) + " " + getString(R.string.bytes);  //$NON-NLS-1$
            } else if (fileSize / 1024 < 1024) {
                return addDotMiles(fileSize / 1024) + " " + getString(R.string.kilobytes);  //$NON-NLS-1$
            } else {
                final long kbs = fileSize / 1024;
                String fraction = Long.toString(kbs % 1024);
                if (fraction.length() > 2) {
                    fraction = fraction.substring(0, 2);
                }
                return addDotMiles(kbs / 1024) + "," + fraction + " " + getString(R.string.megabytes);  //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        /**
         * Devuelve un n&uacute;mero en formato texto con los puntos de miles.
         *
         * @param number N&uacute;mero que se quiere representar.
         * @return N&uacute;mero representado.
         */
        private String addDotMiles(final long number) {

            final String nString = Long.toString(number);
            final StringBuilder buffer = new StringBuilder();
            if (nString.length() > 3) {
                int dotPos = nString.length() % 3;
                if (dotPos > 0) {
                    buffer.append(nString.substring(0, dotPos));
                }
                while (dotPos < nString.length()) {
                    if (dotPos > 0) {
                        buffer.append('.');
                    }
                    buffer.append(nString.substring(dotPos, dotPos + 3));
                    dotPos += 3;
                }

            } else {
                buffer.append(nString);
            }

            return buffer.toString();
        }

        @Override
        public void onClick(final View v) {
            PetitionDetailsActivity.this.selectedDocItem = this;
            showConfirmPreviewDialog();
        }
    }


        /**
         * Adaptador para la lista de l&iacute;neas de firma.
         */
        class SignLineArrayAdapter extends ArrayAdapter<RequestDetailAdapterItem> {

            private final LayoutInflater mInflater;

            private SignLineArrayAdapter(final Context context, final List<RequestDetailAdapterItem> objects) {
                super(context, 0, objects);
                this.mInflater = LayoutInflater.from(context);
            }

            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }

            @Override
            public boolean isEnabled(int position) {
                return false;
            }

            @Override
            public int getViewTypeCount() {
                return SignLineItemType.values().length;
            }

            @Override
            public int getItemViewType(final int position) {
                return getItem(position).getViewType();
            }

            @Override
            public View getView(final int position, final View convertView, final ViewGroup parent) {
                return getItem(position).getView(this.mInflater, convertView);
            }
        }
}