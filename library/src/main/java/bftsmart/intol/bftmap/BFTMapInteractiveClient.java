/**
 * BFT Map implementation (interactive client).
 *
 */
package bftsmart.intol.bftmap;

import java.io.Console;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BFTMapInteractiveClient {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Insert something.");
            System.exit(-1);
        }
        int clientId;
        try {
            clientId = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.exit(-1);
            System.out.println("Insert a number.");
            return;
        }

        if (clientId == 0 || clientId == 1 || clientId == 2 || clientId == 3) {
            System.out.println("No permissions to this port.");
            System.exit(-1);
        }
        BFTMap<Integer, String> bftMapProxy = new BFTMap<>(clientId);
        // cria um objeto da classe BFTMap.
        Console console = System.console();

        System.out.println("\n===== DTI CLIENT MENU =====");
        System.out.println("\nCommands:\n");
        System.out.println("\t0. MY_COINS: Get the IDs and values of the coins associated with this user");
        System.out.println("\t1. MINT: Create a Coin, need certain permissions");
        System.out.println("\t2. SPEND: Make an transaction");
        System.out.println("\t3. MY_NFTS: List the ID, name, URI, and value of the NFTs the issuer possesses");
        System.out.println("\t4. MINT_NFT: Create an NFT for the issuer with the name and uri specified");
        System.out.println("\t5. SET_NFT_PRICE: change the price of a given NFT owned by the issuer to a new value.");
        System.out.println(
                "\t6. SEARCH_NFT: List the id, name, URI, and value of the NFTs whose name contains the provided text.");
        System.out.println("\t7. BUY_NFT: Buy an NFT using coins.");
        System.out.println("\t8. EXIT: Terminate this client\n");

        while (true) {
            String input = console.readLine("\nChoose an option (0-8): ");

            // Verifica se a entrada contém apenas um número de 0 a 6
            if (!input.matches("^[0-8]$")) {
                System.out.println("Error: Please enter a valid number between 0 and 8.");
                continue;
            }
            int cmd = Integer.parseInt(input);
            switch (cmd) {
                case 0: // MY_COINS
                    List<Coin> myCoins = bftMapProxy.my_Coins();
                    if (myCoins == null || myCoins.isEmpty()) {
                        System.out.println("You have no coins.");
                    } else {
                        for (Coin coin : myCoins) {
                            System.out.println("CoinID: " + coin.getId() + " | Value: " + coin.getValue());
                        }
                    }
                    break;
                case 1: // MINT
                    if (clientId == 4) {//fazemos validacao no servidor tambem.
                        try {
                            float valueCoin;
                            while (true) {
                                String value = console.readLine("Enter a value for the coin: ");
                                if (!value.matches("^\\d*\\.?\\d+$")) { // Verifica se a entrada contém apenas números e
                                                                        // um ponto decimal
                                    System.out.println("Error: Please enter a valid number (example: 10 or 10.5).");
                                    continue;
                                }
                                valueCoin = Float.parseFloat(value);
                                if (valueCoin <= 0) {
                                    System.out.println("Error: The value needs to be higher than 0. Try again.");
                                    continue;
                                }
                                break;
                            }
                            int resposta_client = bftMapProxy.mint(valueCoin);
                            System.out.println("The coin ID is: " + resposta_client);
                        } catch (Exception e) {
                            System.out.println("Failed to execute MINT.");
                            System.exit(-1);
                        }
                    } else {
                        System.out.println("You don't have permission.");
                    }
                    break;
                case 2: // SPEND
                    try {
                        String inputCoins;
                        String inputReceiver;
                        String inputValue;
                        do {
                            inputCoins = console.readLine("Enter Coin IDs (comma-separated): ");
                        } while (inputCoins.isEmpty());
                        List<Integer> idCoins = processCoinIds(inputCoins); // da split a mensagem
                        do {
                            inputReceiver = console.readLine("Enter recipient ID: ");// FILTRAR SERVERS
                        } while (inputReceiver.isEmpty());

                        int receiver = Integer.parseInt(inputReceiver);

                        do {
                            inputValue = console.readLine("Enter coin value: ");
                        } while (inputValue.isEmpty());
                        float valueCoin = Float.parseFloat(inputValue);
                        List<Object> respond = bftMapProxy.spend(idCoins, receiver, valueCoin);
                        int resposta_client=(int) respond.get(0);
                        if (resposta_client== 0) {
                            System.out.println("No change: 0");
                        } else if (resposta_client == -1) {
                            System.out.println("Transaction error: -1");
                        } else {
                            System.out.println("CoinID: " + resposta_client + " | Remaining Value: " + respond.get(1));
                        }
                    } catch (Exception e) {
                        System.out.println("Error in SPEND.");
                        System.exit(-1);
                    }
                    break;
                case 3: // MY_NFTS
                    List<NFT> myNfts = bftMapProxy.my_NFTs();
                    if (myNfts == null || myNfts.isEmpty()) {
                        System.out.println("You have no NFTs.");
                    } else {
                        for (NFT nft : myNfts) {
                            System.out.println("NFTID: " + nft.getId() + " | Name: " + nft.getName() + " | URI: "
                                    + nft.getUri() + " | Value: " + nft.getValue());
                        }
                    }
                    break;
                case 4: // MINT_NFT
                    try {
                        String name;
                        String uri;
                        do {
                            name = console.readLine("Enter a name for the NFT: ");
                        } while (name.isEmpty());
                        do {
                            uri = console.readLine("Enter a URI for the NFT: ");
                        } while (uri.isEmpty());
                        float valueNFT;
                        while (true) {
                            String value = console.readLine("Enter a value for the NFT: ");
                            if (!value.matches("^\\d*\\.?\\d+$")) { // Verifica se a entrada contém apenas números e um
                                                                    // ponto decimal
                                System.out.println("Error: Please enter a valid number (example: 10 or 10.5).");
                                continue;
                            }
                            try {
                                valueNFT = Float.parseFloat(value);
                                if (valueNFT <= 0) {
                                    System.out.println("Error: The value needs to be higher than 0. Try again.");
                                    continue;
                                }
                                break;
                            } catch (NumberFormatException e) {
                                System.out.println("Error: Invalid number format. Please enter a valid number.");
                            }
                        }
                        int resposta_client = bftMapProxy.mint_NFT(name, uri, valueNFT);
                        if (resposta_client == -1) {
                            System.out.println("Error: The NFT name is already in use.");
                        } else {
                            System.out.println("The NFT ID is: " + resposta_client);
                        }
                    } catch (Exception e) {
                        System.out.println("Failed to execute MINT_NFT.");
                        System.exit(-1);
                    }
                    break;
                case 5: // SET_NFT_PRICE
                    try {
                        int nftId;
                        while (true) {
                            String id = console.readLine("Enter the NFT ID to update price: ");
                            if (!id.matches("^\\d+$")) { // Verifica se o ID contém apenas números inteiros
                                System.out.println("Error: Please enter a valid positive integer for the NFT ID.");
                                continue;
                            }
                            try {
                                nftId = Integer.parseInt(id);
                                if (nftId < 0) {
                                    System.out.println("Error: NFT ID cannot be negative. Try again.");
                                    continue;
                                }
                                break;
                            } catch (NumberFormatException e) {
                                System.out.println("Error: Invalid ID format. Please enter a valid integer.");
                            }
                        }
                        float valueNFT;
                        while (true) {
                            String value = console.readLine("Enter a new price for the NFT: ");
                            if (!value.matches("^\\d*\\.?\\d+$")) { // Verifica se a entrada contém apenas números e um
                                                                    // ponto decimal // ponto decimal
                                System.out.println("Error: Please enter a valid number (example: 10 or 10.5).");
                                continue;
                            }
                            try {
                                valueNFT = Float.parseFloat(value);
                                if (valueNFT <= 0) {
                                    System.out.println("Error: The value needs to be higher than 0. Try again.");
                                    continue;
                                }
                                break;
                            } catch (NumberFormatException e) {
                                System.out.println("Error: Invalid number format. Please enter a valid number.");
                            }
                        }
                        int resposta_client = bftMapProxy.setNFTPrice(nftId, valueNFT);
                        if (resposta_client == -1) {
                            System.out.println(
                                    "Failed to update NFT price. Make sure you own this NFT and the ID is valid.");
                            continue;
                        } else {
                            System.out.println("NFT price updated successfully!");
                        }
                    } catch (ClassNotFoundException e) {
                        System.out.println("Failed to execute SET_NFT_PRICE.");
                        System.exit(-1);
                    }
                    break;

                case 6: // SEARCH_NFT
                    String searchText;
                    do {
                        searchText = console.readLine("Enter part of the NFT name to search: ");
                    } while (searchText.isEmpty());
                    List<NFT> nftList = bftMapProxy.searchNFT(searchText);
                    if (nftList == null || nftList.isEmpty()) {
                        System.out.println("No NFTs found matching the search criteria.");
                    } else {
                        System.out.println("NFTs found:");
                        for (NFT nft : nftList) {
                            System.out.println("ID: " + nft.getId() + ", Name: " + nft.getName() +
                                    ", URI: " + nft.getUri() + ", Value: " + nft.getValue());
                        }
                    }
                    break;

                case 7: // BUY_NFT
                    try {
                        String inputCoins;
                        String inputNftId;
                        do {
                            inputCoins = console.readLine("Enter Coin IDs (comma-separated): ");
                        } while (inputCoins.isEmpty());
                        List<Integer> coinIds = processCoinIds(inputCoins);
                        do {
                            inputNftId = console.readLine("Enter NFT ID to buy: ");
                        } while (inputNftId.isEmpty());
                        int nftId = Integer.parseInt(inputNftId);
                        List<Object> respond = bftMapProxy.buyNFT(nftId, coinIds);
                        int result=(int) respond.get(0);
                        if (result == -1) {
                            System.out.println("Transaction failed.");
                        } else if (result == 0) {
                            System.out.println("Transaction successful. No change.");
                        } else {
                            System.out.println("Transaction successful. Change coin ID: " + result);
                        }
                    } catch (Exception e) {
                        System.out.println("Error in BUY_NFT.");
                    }
                    break;

                case 8: // Exit
                    System.out.println("Exiting... Bye!");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid command.");
                    break;
            }
        }
    }
    public static List<Integer> processCoinIds(String input) {
        List<Integer> coinIds = new ArrayList<>();
        int checkID;
        for (String id : input.split("\\s*,\\s*")) {
            try {
                checkID = Integer.parseInt(id);// verifica se oq escreveu é um int
                coinIds.add(checkID); // adiciona a lista
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid number (example: 10 or 10.5).");
                System.out.println("Try again");
                System.exit(-1);
            }
        }
        return coinIds;
    }
}
