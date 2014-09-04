package com.mycelium.wapi.wallet;

import com.mrd.bitlib.model.OutPoint;
import com.mrd.bitlib.util.Sha256Hash;
import com.mycelium.wapi.model.TransactionEx;
import com.mycelium.wapi.model.TransactionOutputEx;
import com.mycelium.wapi.wallet.bip44.Bip44AccountContext;
import com.mycelium.wapi.wallet.single.SingleAddressAccountContext;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AccountBacking {

   void beginTransaction();

   void setTransactionSuccessful();

   void endTransaction();

   void clear();

   Collection<TransactionOutputEx> getAllUnspentOutputs();

   TransactionOutputEx getUnspentOutput(OutPoint outPoint);

   void deleteUnspentOutput(OutPoint outPoint);

   void putUnspentOutput(TransactionOutputEx output);

   void putParentTransactionOutput(TransactionOutputEx output);

   TransactionOutputEx getParentTransactionOutput(OutPoint outPoint);

   boolean hasParentTransactionOutput(OutPoint outPoint);

   void putTransaction(TransactionEx transaction);

   TransactionEx getTransaction(Sha256Hash hash);

   void deleteTransaction(Sha256Hash hash);

   List<TransactionEx> getTransactionHistory(int offset, int limit);

   Collection<TransactionEx> getUnconfirmedTransactions();

   Collection<TransactionEx> getYoungTransactions(int maxConfirmations, int blockChainHeight);

   boolean hasTransaction(Sha256Hash txid);

   void putOutgoingTransaction(Sha256Hash txid, byte[] rawTransaction);

   List<byte[]> getOutgoingTransactions();

   boolean isOutgoingTransaction(Sha256Hash txid);

   void removeOutgoingTransaction(Sha256Hash txid);

}