package es.gob.afirma.android.signfolder;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

final class LoginOptionsDialogBuilder {

	private final LoginOptionsListener listener;

	private final AlertDialog.Builder builder;

	private final AlertDialog alertDialog;
	AlertDialog getAlertDialog() {
		return this.alertDialog;
	}

	private CharSequence[] items;
	CharSequence[] getItems() {
		return this.items;
	}

	private int selectedServer;
	int getSelectedServer() {
		return this.selectedServer;
	}
	void setSelectedServer(final int s) {
		this.selectedServer = s;
	}

	LoginOptionsDialogBuilder(final Activity activity, final LoginOptionsListener listener) {

		this.listener = listener;
		this.builder = new AlertDialog.Builder(activity);
		final LayoutInflater inflater = activity.getLayoutInflater();

		List<String> servers = AppPreferences.getInstance().getServersList();

		// Si no hay ningun servidor configurado, reestablecemos los por defecto
		if (servers.isEmpty()) {
			AppPreferences.getInstance().setDefaultServers();
			servers = AppPreferences.getInstance().getServersList();
		}

		// Ordenamos por alias
		Collections.sort(servers);
		this.items = servers.toArray(new CharSequence[servers.size()]);
		this.selectedServer = servers.indexOf(AppPreferences.getInstance().getSelectedProxyAlias());

		// Si no hay ningun servidor marcado como por defecto, marcamos el primero
		if (this.selectedServer == -1) {
			this.selectedServer = 0;
			String selectedServerAlias = this.items[this.selectedServer].toString();
			AppPreferences.getInstance().setSelectedProxy(
					selectedServerAlias,
					AppPreferences.getInstance().getServer(selectedServerAlias));
		}

		// Mostramos el dialogo con los servidores
		this.builder.setCustomTitle(inflater.inflate(R.layout.dialog_server_title, null));
		this.builder.setSingleChoiceItems(this.items, this.selectedServer, new OnClickListener() {
			@Override
			public void onClick(final DialogInterface d, final int n) {
				setSelectedServer(n);
			}
		});

		this.builder.setPositiveButton(R.string.ok,
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(final DialogInterface dialog, final int which) {
					final String selectedAlias = getItems()[getSelectedServer()].toString();
					AppPreferences.getInstance().setSelectedProxy(
							selectedAlias,
							AppPreferences.getInstance().getServer(selectedAlias)
					);
				}
			}
		);

		this.builder.setNeutralButton(R.string.dialog_server_new_button,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						addServer(activity, inflater);
					}
			}
		);
		this.builder.setNegativeButton(R.string.cancel, null);
		this.alertDialog = this.builder.create();

		if (!servers.isEmpty()) {
			this.alertDialog.setOnShowListener(new OnShowListener()
			{
			    @Override
			public void onShow(final DialogInterface dialog)
			{
			        final ListView lv = getAlertDialog().getListView();
			        lv.setOnItemLongClickListener(new OnItemLongClickListener()
			    {
			    @Override
			    public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id)
			    {
			    	editServer(
		    			activity,
		    			inflater,
		    			getItems()[position].toString(),
		    			AppPreferences.getInstance().getServer(getItems()[position].toString())
			    	);
					getAlertDialog().dismiss();
					return true;
			    }
			    });
			}
			});
		}
	}

	/**
	 * Muestra un di&aacute;logo para agregar un nuevo servidor proxy al listado de servidores.
	 * @param act Actividad sobre la que se muestra el di&aacute;logo.
	 * @param inflater Para la edici&oacute;n del layout del di&aacute;logo.
	 */
	private void addServer(final Activity act, final LayoutInflater inflater) {

		final View view = inflater.inflate(R.layout.dialog_add_server, null);

		final EditText aliasField = (EditText) view.findViewById(R.id.alias);
		final EditText urlField = (EditText) view.findViewById(R.id.url);
		aliasField.requestFocus();

		final AlertDialog dialog = new AlertDialog.Builder(act)
				.setView(view)
				.setTitle(R.string.dialog_add_server_title)
				.setPositiveButton(R.string.ok, null)
				.setNegativeButton(R.string.cancel, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						// Abrimos el dialogo de seleccion
						final LoginOptionsDialogBuilder dialogBuilder = new LoginOptionsDialogBuilder(act, getListener());
						dialogBuilder.show();
					}
				}).create();

		dialog.show();

		// Definimos un comportamiento especial en el boton de aceptar para que se validen los
		// campos sin que se cierre el dialogo
		final Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
		positiveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String newAlias = aliasField.getText().toString().trim();
				String newUrl = urlField.getText().toString().trim();

				// Comprobamos que no haya campos vacios
				if (newAlias.isEmpty() || newUrl.isEmpty()) {
					getListener().onErrorLoginOptions(act.getString(R.string.dialog_server_empty_fields));
					return;
				}

				// Comprobamos que el nuevo alias no pise a algun otro
				if (!AppPreferences.getInstance().getServer(newAlias).isEmpty()) {
					getListener().onErrorLoginOptions(act.getString(R.string.dialog_server_duplicated));
					return;
				}

				// Comprobamos que la URL este bien formada
				try {
					new URL(newUrl).toString();
				}
				catch (final Exception e) {
					getListener().onErrorLoginOptions(act.getString(R.string.invalid_url));
					return;
				}

				// Guardamos la informacion del nuevo servidor
				AppPreferences.getInstance().saveServer(newAlias, newUrl);

				// Establecemos este servidor como el seleccionado
				AppPreferences.getInstance().setSelectedProxy(newAlias, newUrl);

				// Cerramos el dialogo de edicion y abrimos el de seleccion
				dialog.dismiss();
				final LoginOptionsDialogBuilder dialogBuilder = new LoginOptionsDialogBuilder(act, getListener());
				dialogBuilder.show();
			}
		});
	}

	void editServer(final Activity act, final LayoutInflater inflater, final String alias, final String url) {

		final View view = inflater.inflate(R.layout.dialog_add_server, null);

		final EditText aliasField = (EditText) view.findViewById(R.id.alias);
		final EditText urlField = (EditText) view.findViewById(R.id.url);

		aliasField.setText(alias);
		urlField.setText(url);

		aliasField.requestFocus();

		final AlertDialog dialog = new AlertDialog.Builder(act)
				.setView(view)
				.setTitle(R.string.dialog_edit_server_title)
				.setPositiveButton(R.string.ok, null)
				.setNegativeButton(R.string.cancel, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						// Abrimos el dialogo de seleccion
						final LoginOptionsDialogBuilder dialogBuilder = new LoginOptionsDialogBuilder(act, getListener());
						dialogBuilder.show();
					}
				})
				.setNeutralButton(R.string.dialog_server_delete_button, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						// Eliminamos el proxy
						AppPreferences.getInstance().removeServer(alias);
						// Si era el proxy seleccionado, eliminamos la configuracion
						if (AppPreferences.getInstance().getSelectedProxyAlias().equals(alias)) {
							AppPreferences.getInstance().removeProxyConfig();
						}
						// Abrimos el dialogo de seleccion
						final LoginOptionsDialogBuilder dialogBuilder = new LoginOptionsDialogBuilder(act, getListener());
						dialogBuilder.show();
					}
				}).create();

		dialog.show();

		// Definimos un comportamiento especial en el boton de aceptar para que se validen los
		// campos sin que se cierre el dialogo
		final Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
		positiveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				String newAlias = aliasField.getText().toString().trim();
				String newUrl = urlField.getText().toString().trim();

				// Comprobamos que no haya campos vacios
				if (newAlias.isEmpty() || newUrl.isEmpty()) {
					getListener().onErrorLoginOptions(act.getString(R.string.dialog_server_empty_fields));
					return;
				}

				// Comprobamos que, si se ha cambiado el alias, no pise a algun otro
				if (!newAlias.equals(alias) &&
						!AppPreferences.getInstance().getServer(newAlias).isEmpty()) {
					getListener().onErrorLoginOptions(act.getString(R.string.dialog_server_duplicated));
					return;
				}

				// Comprobamos que la URL este bien formada
				try {
					new URL(newUrl).toString();
				}
				catch (final Exception e) {
					getListener().onErrorLoginOptions(act.getString(R.string.invalid_url));
					return;
				}

				// Eliminamos la configuracion de servidor anterior y guardamos la nueva
				AppPreferences.getInstance().removeServer(alias);
				AppPreferences.getInstance().saveServer(newAlias, newUrl);

				// Establecemos este servidor como el seleccionado
				AppPreferences.getInstance().setSelectedProxy(newAlias, newUrl);

				// Cerramos el dialogo de edicion y abrimos el de seleccion
				dialog.dismiss();
				final LoginOptionsDialogBuilder dialogBuilder = new LoginOptionsDialogBuilder(act, getListener());
				dialogBuilder.show();
			}
		});

	}

	LoginOptionsListener getListener() {
		return this.listener;
	}

	public void show() {
		this.alertDialog.show();
	}

	/**
	 * Interfaz a la que se notifica cuando ocurre un error en la configuraci&oacute;n de la
	 * aplicaci&oacute;n.
	 */
	public interface LoginOptionsListener {

		void onErrorLoginOptions(final String url);
	}
}
