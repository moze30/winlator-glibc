package com.winlator.glibc.contentdialog;

import android.view.View;

/**
 * @deprecated This dialog is no longer used. VKD3D configuration has been merged into {@link DXVKConfigDialog}.
 */
@Deprecated
public class VKD3DConfigDialog extends ContentDialog {
    public VKD3DConfigDialog(View anchor) {
        super(anchor.getContext(), 0);
    }

    public static void setEnvVars(android.content.Context context, com.winlator.glibc.core.KeyValueSet config, com.winlator.glibc.core.EnvVars envVars) {
        DXVKConfigDialog.setEnvVars(context, config, envVars);
    }
}
