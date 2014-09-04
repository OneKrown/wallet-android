/*
 * Copyright 2013 Megion Research and Development GmbH
 *
 * Licensed under the Microsoft Reference Source License (MS-RSL)
 *
 * This license governs use of the accompanying software. If you use the software, you accept this license.
 * If you do not accept the license, do not use the software.
 *
 * 1. Definitions
 * The terms "reproduce," "reproduction," and "distribution" have the same meaning here as under U.S. copyright law.
 * "You" means the licensee of the software.
 * "Your company" means the company you worked for when you downloaded the software.
 * "Reference use" means use of the software within your company as a reference, in read only form, for the sole purposes
 * of debugging your products, maintaining your products, or enhancing the interoperability of your products with the
 * software, and specifically excludes the right to distribute the software outside of your company.
 * "Licensed patents" means any Licensor patent claims which read directly on the software as distributed by the Licensor
 * under this license.
 *
 * 2. Grant of Rights
 * (A) Copyright Grant- Subject to the terms of this license, the Licensor grants you a non-transferable, non-exclusive,
 * worldwide, royalty-free copyright license to reproduce the software for reference use.
 * (B) Patent Grant- Subject to the terms of this license, the Licensor grants you a non-transferable, non-exclusive,
 * worldwide, royalty-free patent license under licensed patents for reference use.
 *
 * 3. Limitations
 * (A) No Trademark License- This license does not grant you any rights to use the Licensor’s name, logo, or trademarks.
 * (B) If you begin patent litigation against the Licensor over patents that you think may apply to the software
 * (including a cross-claim or counterclaim in a lawsuit), your license to the software ends automatically.
 * (C) The software is licensed "as-is." You bear the risk of using it. The Licensor gives no express warranties,
 * guarantees or conditions. You may have additional consumer rights under your local laws which this license cannot
 * change. To the extent permitted under your local laws, the Licensor excludes the implied warranties of merchantability,
 * fitness for a particular purpose and non-infringement.
 */

package com.mycelium.wallet.activity.modern;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mycelium.wallet.MbwManager;
import com.mycelium.wallet.R;
import com.mycelium.wallet.Utils;
import com.mycelium.wallet.persistence.MetadataStorage;
import com.mycelium.wapi.model.Balance;
import com.mycelium.wapi.wallet.WalletAccount;
import com.mycelium.wapi.wallet.bip44.Bip44Account;

public class RecordRowBuilder {

   private final MbwManager mbwManager;
   private final Resources resources;
   private final LayoutInflater inflater;

   public RecordRowBuilder(MbwManager mbwManager, Resources resources, LayoutInflater inflater) {
      this.mbwManager = mbwManager;
      this.resources = resources;
      this.inflater = inflater;
   }

   public View buildRecordView(ViewGroup parent, WalletAccount walletAccount, boolean isSelected, boolean hasFocus) {
      View rowView = inflater.inflate(R.layout.record_row, parent, false);

      // Make grey if not part of the balance
      if (!isSelected) {
         Utils.setAlpha(rowView, 0.5f);
      }

      int textColor = resources.getColor(R.color.white);

      // Show focus if applicable
      if (hasFocus) {
         rowView.setBackgroundColor(resources.getColor(R.color.selectedrecord));
      }

      // Show/hide key icon
      if (!walletAccount.canSpend()) {
         rowView.findViewById(R.id.ivKey).setVisibility(View.INVISIBLE);
         rowView.findViewById(R.id.ivMultipleKeys).setVisibility(View.INVISIBLE);
      } else if (walletAccount instanceof Bip44Account) {
         rowView.findViewById(R.id.ivKey).setVisibility(View.INVISIBLE);
         rowView.findViewById(R.id.ivMultipleKeys).setVisibility(View.VISIBLE);
      } else {
         rowView.findViewById(R.id.ivKey).setVisibility(View.VISIBLE);
         rowView.findViewById(R.id.ivMultipleKeys).setVisibility(View.INVISIBLE);
      }

      String name = mbwManager.getMetadataStorage().getLabelByAccount(walletAccount.getId());
      if (name.length() == 0) {
         ((TextView) rowView.findViewById(R.id.tvLabel)).setVisibility(View.GONE);
      } else {
         // Display name
         TextView tvLabel = ((TextView) rowView.findViewById(R.id.tvLabel));
         tvLabel.setVisibility(View.VISIBLE);
         tvLabel.setText(name);
         tvLabel.setTextColor(textColor);
      }

      String displayAddress;
      if (walletAccount instanceof Bip44Account) {
         if (walletAccount.isActive()) {
            int numKeys = ((Bip44Account) walletAccount).getPrivateKeyCount();
            if (numKeys > 1) {
               displayAddress = resources.getString(R.string.contains_keys, numKeys);
            } else {
               displayAddress = resources.getString(R.string.contains_one_key);
            }
         } else {
            displayAddress = ""; //dont show key count of archived accs
         }
      } else {
         if (name.length() == 0 && walletAccount.isActive()) {
            // Display address in it's full glory, chopping it into three
            displayAddress = walletAccount.getReceivingAddress().toMultiLineString();
         } else {
            // Display address in short form
            displayAddress = getShortAddress(walletAccount.getReceivingAddress().toString());
         }
      }


      TextView tvAddress = ((TextView) rowView.findViewById(R.id.tvAddress));
      tvAddress.setText(displayAddress);
      tvAddress.setTextColor(textColor);

      // Set tag
      rowView.setTag(walletAccount);

      // Set balance
      if (walletAccount.isActive()) {
         Balance balance = walletAccount.getBalance();
         rowView.findViewById(R.id.tvBalance).setVisibility(View.VISIBLE);
         String balanceString = mbwManager.getBtcValueString(balance.confirmed + balance.pendingChange);
         TextView tvBalance = ((TextView) rowView.findViewById(R.id.tvBalance));
         tvBalance.setText(balanceString);
         tvBalance.setTextColor(textColor);
      } else {
         // We don't show anything if the account is archived
         rowView.findViewById(R.id.tvBalance).setVisibility(View.GONE);
      }

      boolean needsBackupVerification = walletAccount.canSpend() && mbwManager.getMetadataStorage().getBackupState(walletAccount).equals(MetadataStorage.BackupState.UNKNOWN);
      if (needsBackupVerification) {
         rowView.findViewById(R.id.tvNoBackupWarning).setVisibility(View.VISIBLE);
      } else {
         rowView.findViewById(R.id.tvNoBackupWarning).setVisibility(View.GONE);
      }

      // Show/hide trader account message
      if (walletAccount.getId().equals(mbwManager.getLocalTraderManager().getLocalTraderAccountId())) {
         rowView.findViewById(R.id.tvTraderKey).setVisibility(View.VISIBLE);
      } else {
         rowView.findViewById(R.id.tvTraderKey).setVisibility(View.GONE);
      }

      return rowView;
   }

   private static String getShortAddress(String addressString) {
      StringBuilder sb = new StringBuilder();
      sb.append(addressString.substring(0, 6));
      sb.append("...");
      sb.append(addressString.substring(addressString.length() - 6));
      return sb.toString();
   }

}
