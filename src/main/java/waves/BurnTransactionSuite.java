package waves;

import com.wavesplatform.wavesj.Node;
import com.wavesplatform.wavesj.PrivateKeyAccount;
import com.wavesplatform.wavesj.Transaction;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

import java.io.IOException;

import static waves.TestVariables.*;

import java.net.URISyntaxException;

public class BurnTransactionSuite {

    private final String nodeUrl;
    private final Node node;
    private final static long FEE = 100_000;
    private final CloseableHttpClient client = HttpClients.createDefault();

    private final PrivateKeyAccount account;
    private final String assetId;

    public BurnTransactionSuite() throws IOException, URISyntaxException {
        System.setProperty("env", "env_qa");
        nodeUrl = getProtocol() + getHost();
        node = new Node(nodeUrl);
        account = PrivateKeyAccount.fromSeed(getSeed(), 0, 'T');
        assetId = getAssetId();
    }

    @Test
    public void test() throws IOException {
        long balance0 = node.getBalance(account.getAddress(), assetId);

        Transaction burnTx = Transaction.makeBurnTx(account, assetId, 1, FEE);
        String burnTxId = node.send(burnTx);
        System.out.println("tx id: " + burnTxId);

        waitForTransaction(burnTxId);
        long balance1 = node.getBalance(account.getAddress(), assetId);
        assertThat(balance1, equalTo(balance0 - 1));
    }

    private void waitForTransaction(String txId) throws IOException {
        while (true) {
            HttpUriRequest req = new HttpGet(nodeUrl + "/transactions/info/" + txId);
            HttpResponse resp = client.execute(req);
            int status = resp.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore and wait further
            }
        }
    }
}