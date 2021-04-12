/*
-- CREATION 
---------------------------------------------------------------------------
-- TYPE: Classe java 
-- CRÉÉ PAR: 
-- CRÉÉ LE : 20/06/2014
-- DESCRIPTION DU SPECIFIQUE :
-- 	VERSION: 1.0 
--	 ENTREE :
--		  - PARAMETRES
--		  - FICHIERS
--		  - TABLES
-- SORTIE :
--   - PARAMETRES
--		  - FICHIERS
--		  - TABLES
---------------------------------------------------------------------------
-- MODIFICATIONS ****************************************************
---------------------------------------------------------------------------
-- MODIFIÉ PAR : Gilles Roussel
-- MODIFIÉ LE:  20/06/2014
-- VERSION: 1.1
-- OBJET MODIFICATION : 
--  Contournement du defect détacté lors du test des paiements AP via KYRIBA et EIGERPAY sur le cloud
-- 	Classe java implémentant un nouveau Transfert Propocol
--  La fonction transmit inclu un appel à la nouvelle fonction xxst_chmod qui modifie les permissions sur le fichier nouvellement créé.
---------------------------------------------------------------------------
*/

package xxst.oracle.apps.iby.net;

import javax.naming.*;
import javax.naming.directory.*;
import javax.security.auth.login.*;
import javax.security.auth.Subject;
import java.util.Hashtable;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.sql.*;
import java.util.*;
import oracle.apps.fnd.common.EnvironmentStore;
import oracle.apps.fnd.common.VersionInfo;
import oracle.apps.fnd.cp.request.*;
import oracle.apps.fnd.util.NameValueType;
import oracle.apps.fnd.util.ParameterList;
import oracle.apps.iby.database.DBWrapper;
import oracle.apps.iby.ecapp.OraPmt;
import oracle.apps.iby.engine.TransmitConfig;
import oracle.apps.iby.exception.*;
import oracle.apps.iby.init.ContextRegistry;
import oracle.apps.iby.payment.FundDisbursementProfile;
import oracle.apps.iby.payment.FundsDisbursementTrxnContext;
import oracle.apps.iby.util.OAAttachmentServerUtilsWrapper;
import oracle.apps.iby.util.bpsUtil;
import java.io.*;
import java.util.Dictionary;
import oracle.apps.fnd.common.VersionInfo;
import oracle.apps.iby.engine.CodePoint;
import oracle.apps.iby.exception.Log;
import oracle.apps.iby.exception.PSException;
import oracle.apps.iby.util.bpsUtil;
import oracle.apps.xdo.delivery.DeliveryManager;
import oracle.apps.xdo.delivery.DeliveryRequest;
import oracle.apps.iby.net.FileDumpFunction;

public class XXST_KYRIBA_FileDumpFunction extends FileDumpFunction {

	public XXST_KYRIBA_FileDumpFunction() {
		super();
	}

	public InputStream transmit(Dictionary params, InputStream payload)
			throws PSException {
		boolean test = true;

		String s = "iby.net.XXST_KYRIBA_FileDumpFunction.transmit";

		String fname = "";

		if (test)
			try {
				fname = (String) params.get("TRANSMIT_REF");
			} catch (Exception ex) {
				// test = false;
				throw new PSException("IBY_0001");
			}

		if (test)
			try {
				Connection m_conn = DBWrapper.getDBConnection();
				m_conn.setAutoCommit(false);

				PreparedStatement preparedstatement = m_conn
						.prepareStatement("select xxst_ce02_pkg.get_file_name(:1) from dual ");
				preparedstatement.setString(1, fname);

				ResultSet resultset = preparedstatement.executeQuery();
				if (resultset.next())
					fname = resultset.getString(1);

				DBWrapper.closeResultSet(resultset);
				DBWrapper.closeStatement(preparedstatement);
			} catch (Exception ex) {
				// test = false;
				throw new PSException("IBY_0001");
			}

		if (test)
			try {
				params.remove("TRANSMIT_REF");
				params.put("TRANSMIT_REF", fname);
				params.remove("FILE_NAME");
				params.put("FILE_NAME", fname);
			} catch (Exception ex) {
				// test = false;
				throw new PSException("IBY_0001");
			}

		if (test)
			try {
				String fpath = (String) params.get("FILE_DIR");
				String flag = bpsUtil.NVL((String) params.get("APPEND_FLAG"), "Y");
				FileOutputStream fileoutputstream = new FileOutputStream(
						getFileFullName(fpath, fname), !"N".equals(flag));
				dumpFile(payload, fileoutputstream);
				// Contournement SR wrong permisssions: modification explicite
				// des permissions sur le fichier dans le dossier export FTP
				fileoutputstream.close();
				xxst_chmod(getFileFullName(fpath, fname));
				// FIN Contournement SR wrong permisssions
			} catch (IOException ioexception) {
				// test = false;
				throw new PSException("IBY_0001");
			} catch (Exception ex) {
				// test = false;
				throw new PSException("IBY_0001");
			}

		return new ByteArrayInputStream(NULL_ACK);
	}

	public static void dumpFile(InputStream inputstream,
			OutputStream outputstream) throws IOException {
		byte abyte0[] = new byte[512];
		int i;
		while ((i = inputstream.read(abyte0)) > 0)
			outputstream.write(abyte0, 0, i);
	}

	public static void xxst_chmod(String fileName) throws IOException {
		Runtime runtime = Runtime.getRuntime();
		String[] params = { "chmod", "644", fileName };
		runtime.exec(params);
	}

	private static String getFileFullName(String s, String s1) {
		String s2 = "iby.net.XXST_KYR_FileDumpFunction.getFileFullName";
		Log.debug("fileFullName(): generating file full name", 1, s2);
		if (s != null && !s.endsWith(File.separator))
			return (new StringBuilder()).append(s).append(File.separator)
					.append(s1).toString();
		else
			return (new StringBuilder()).append(s).append(s1).toString();
	}

	protected File makeStageFile(InputStream inputstream, Dictionary dictionary)
			throws IOException {
		String s = "iby..net.FTPFunction.makeStageFile";
		String s1 = (String) dictionary.get("FILE_DIR");
		String s2 = (String) dictionary.get("TRANSMIT_REF");
		String s3;

		if (s1.endsWith(File.separator))
			s3 = (new StringBuilder()).append(s1).append(s2).toString();
		else
			s3 = (new StringBuilder()).append(s1).append(File.separator)
					.append(s2).toString();

		return makeStageFile(inputstream, s3);
	}

	protected File makeStageFile(InputStream inputstream, String s)
			throws IOException {
		String s1 = "iby..net.FTPFunction.makeStageFile";
		if (Log.isEnabled(s1, 1))
			Log.debug((new StringBuilder()).append("local file is ").append(s)
					.toString(), 1, s1);

		File file = new File(s);
		FileOutputStream fileoutputstream = new FileOutputStream(file, false);
		for (int i = inputstream.read(); i != -1; i = inputstream.read())
			fileoutputstream.write(i);

		Log.debug("done file dump", 1, s1);
		fileoutputstream.flush();
		fileoutputstream.close();

		return file;
	}

	static final byte NULL_ACK[] = new byte[0];
}
