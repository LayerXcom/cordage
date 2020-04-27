package jp.co.layerx.cordage.crosschainatomicswap.ethWrapper;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.5.15.
 */
@SuppressWarnings("rawtypes")
public class Settlement extends Contract {
    public static final String BINARY = "0x60806040523480156200001157600080fd5b50604051620016543803806200165483398181016040526200003791908101906200015a565b336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16600073ffffffffffffffffffffffffffffffffffffffff167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e060405160405180910390a380600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550600160038190555050620001d4565b6000815190506200015481620001ba565b92915050565b6000602082840312156200016d57600080fd5b60006200017d8482850162000143565b91505092915050565b600062000193826200019a565b9050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b620001c58162000186565b8114620001d157600080fd5b50565b61147080620001e46000396000f3fe6080604052600436106100705760003560e01c80638f32d59b1161004e5780638f32d59b146100f8578063a96ce7aa14610123578063b7c958721461014c578063f2fde38b1461016857610070565b8063534b7fcd14610075578063715018a6146100b65780638da5cb5b146100cd575b600080fd5b34801561008157600080fd5b5061009c60048036036100979190810190610ad0565b610191565b6040516100ad959493929190611001565b60405180910390f35b3480156100c257600080fd5b506100cb61022a565b005b3480156100d957600080fd5b506100e2610330565b6040516100ef9190610fe6565b60405180910390f35b34801561010457600080fd5b5061010d610359565b60405161011a9190611054565b60405180910390f35b34801561012f57600080fd5b5061014a60048036036101459190810190610b11565b6103b0565b005b61016660048036036101619190810190610b52565b610637565b005b34801561017457600080fd5b5061018f600480360361018a9190810190610aa7565b61083f565b005b6002818051602081018201805184825260208301602085012081835280955050505050506000915090508060000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16908060010160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16908060020154908060030154908060040160009054906101000a900460ff16905085565b610232610359565b610271576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610268906110ef565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff166000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e060405160405180910390a360008060006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905090565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614905090565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614610440576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016104379061108f565b60405180910390fd5b60006002826040516104529190610fcf565b908152602001604051809103902090506000600281111561046f57fe5b8160040160009054906101000a900460ff16600281111561048c57fe5b14156104cd576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016104c49061112f565b60405180910390fd5b600160028111156104da57fe5b8160040160009054906101000a900460ff1660028111156104f757fe5b14610537576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161052e9061110f565b60405180910390fd5b8060010160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc82600201549081150290604051600060405180830381858888f1935050505061059d57600080fd5b60028160040160006101000a81548160ff021916908360028111156105be57fe5b02179055506060816040516020016105d6919061114f565b60405160208183030381529060405290507f7e646be22d23cac996ea886cf1f7bc6b43310119eeee9c8d65cf8e8008af99266003600081548092919060010191905055848360405161062a9392919061116a565b60405180910390a1505050565b8373ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16146106a5576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161069c906110cf565b60405180910390fd5b8134146106e7576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016106de906110af565b60405180910390fd5b60006002866040516106f99190610fcf565b90815260200160405180910390209050848160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550838160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555082816002018190555081816003018190555060018160040160006101000a81548160ff021916908360028111156107c257fe5b02179055506060816040516020016107da919061114f565b60405160208183030381529060405290507fe7e61de6a45eb1bdc4b022cd7eaf8344da1283e4531bcbe37baeba6cbf6524766003600081548092919060010191905055888360405161082e9392919061116a565b60405180910390a150505050505050565b610847610359565b610886576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161087d906110ef565b60405180910390fd5b61088f81610892565b50565b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff161415610902576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016108f99061106f565b60405180910390fd5b8073ffffffffffffffffffffffffffffffffffffffff166000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e060405160405180910390a3806000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555050565b6000813590506109cf816113e8565b92915050565b6000813590506109e4816113ff565b92915050565b600082601f8301126109fb57600080fd5b8135610a0e610a09826111dc565b6111af565b91508082526020830160208301858383011115610a2a57600080fd5b610a3583828461132d565b50505092915050565b600082601f830112610a4f57600080fd5b8135610a62610a5d82611208565b6111af565b91508082526020830160208301858383011115610a7e57600080fd5b610a8983828461132d565b50505092915050565b600081359050610aa181611416565b92915050565b600060208284031215610ab957600080fd5b6000610ac7848285016109c0565b91505092915050565b600060208284031215610ae257600080fd5b600082013567ffffffffffffffff811115610afc57600080fd5b610b08848285016109ea565b91505092915050565b600060208284031215610b2357600080fd5b600082013567ffffffffffffffff811115610b3d57600080fd5b610b4984828501610a3e565b91505092915050565b600080600080600060a08688031215610b6a57600080fd5b600086013567ffffffffffffffff811115610b8457600080fd5b610b9088828901610a3e565b9550506020610ba1888289016109d5565b9450506040610bb2888289016109d5565b9350506060610bc388828901610a92565b9250506080610bd488828901610a92565b9150509295509295909350565b610bea816112c0565b82525050565b610bf9816112c0565b82525050565b610c08816112ae565b82525050565b610c17816112d2565b82525050565b6000610c2882611234565b610c32818561124a565b9350610c4281856020860161133c565b610c4b816113bd565b840191505092915050565b610c5f8161131b565b82525050565b610c6e8161131b565b82525050565b6000610c7f8261123f565b610c89818561125b565b9350610c9981856020860161133c565b610ca2816113bd565b840191505092915050565b6000610cb88261123f565b610cc2818561126c565b9350610cd281856020860161133c565b80840191505092915050565b6000610ceb60268361125b565b91507f4f776e61626c653a206e6577206f776e657220697320746865207a65726f206160008301527f64647265737300000000000000000000000000000000000000000000000000006020830152604082019050919050565b6000610d51601e8361125b565b91507f63616c6c6572206973206e6f742074686520636f726461206e6f7461727900006000830152602082019050919050565b6000610d9160298361125b565b91507f6d73672e76616c7565206973206e6f74206571756976616c656e7420746f205f60008301527f776569416d6f756e7400000000000000000000000000000000000000000000006020830152604082019050919050565b6000610df760268361125b565b91507f6d73672e73656e646572206973206e6f74205f7472616e7366657246726f6d4160008301527f64647265737300000000000000000000000000000000000000000000000000006020830152604082019050919050565b6000610e5d60208361125b565b91507f4f776e61626c653a2063616c6c6572206973206e6f7420746865206f776e65726000830152602082019050919050565b6000610e9d601f8361125b565b91507f7377617044657461696c2e737461747573206973206e6f74204c6f636b6564006000830152602082019050919050565b6000610edd60198361125b565b91507f7377617044657461696c20646f6573206e6f74206578697374000000000000006000830152602082019050919050565b60a082016000808301549050610f258161136f565b610f326000860182610be1565b5060018301549050610f438161136f565b610f506020860182610be1565b5060028301549050610f61816113a3565b610f6e6040860182610fb1565b5060038301549050610f7f816113a3565b610f8c6060860182610fb1565b5060048301549050610f9d81611389565b610faa6080860182610c56565b5050505050565b610fba81611311565b82525050565b610fc981611311565b82525050565b6000610fdb8284610cad565b915081905092915050565b6000602082019050610ffb6000830184610bff565b92915050565b600060a0820190506110166000830188610bf0565b6110236020830187610bf0565b6110306040830186610fc0565b61103d6060830185610fc0565b61104a6080830184610c65565b9695505050505050565b60006020820190506110696000830184610c0e565b92915050565b6000602082019050818103600083015261108881610cde565b9050919050565b600060208201905081810360008301526110a881610d44565b9050919050565b600060208201905081810360008301526110c881610d84565b9050919050565b600060208201905081810360008301526110e881610dea565b9050919050565b6000602082019050818103600083015261110881610e50565b9050919050565b6000602082019050818103600083015261112881610e90565b9050919050565b6000602082019050818103600083015261114881610ed0565b9050919050565b600060a0820190506111646000830184610f10565b92915050565b600060608201905061117f6000830186610fc0565b81810360208301526111918185610c74565b905081810360408301526111a58184610c1d565b9050949350505050565b6000604051905081810181811067ffffffffffffffff821117156111d257600080fd5b8060405250919050565b600067ffffffffffffffff8211156111f357600080fd5b601f19601f8301169050602081019050919050565b600067ffffffffffffffff82111561121f57600080fd5b601f19601f8301169050602081019050919050565b600081519050919050565b600081519050919050565b600082825260208201905092915050565b600082825260208201905092915050565b600081905092915050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b600060ff82169050919050565b6000819050919050565b60006112b9826112f1565b9050919050565b60006112cb826112f1565b9050919050565b60008115159050919050565b60008190506112ec826113db565b919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b6000611326826112de565b9050919050565b82818337600083830152505050565b60005b8381101561135a57808201518184015260208101905061133f565b83811115611369576000848401525b50505050565b600061138261137d836113ce565b611277565b9050919050565b600061139c611397836113ce565b611297565b9050919050565b60006113b66113b1836113ce565b6112a4565b9050919050565b6000601f19601f8301169050919050565b60008160001c9050919050565b600381106113e557fe5b50565b6113f1816112ae565b81146113fc57600080fd5b50565b611408816112c0565b811461141357600080fd5b50565b61141f81611311565b811461142a57600080fd5b5056fea365627a7a72315820abc6d830e1bceb924521d3b3fb85218521ca9ae23ae353ed32619e8440ce9a676c6578706572696d656e74616cf564736f6c63430005110040";

    public static final String FUNC_ISOWNER = "isOwner";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_SWAPIDTODETAILMAP = "swapIdToDetailMap";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final String FUNC_LOCK = "lock";

    public static final String FUNC_UNLOCK = "unlock";

    public static final Event LOCKED_EVENT = new Event("Locked", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<DynamicBytes>() {}));
    ;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event UNLOCKED_EVENT = new Event("Unlocked", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<DynamicBytes>() {}));
    ;

    protected static final HashMap<String, String> _addresses;

    static {
        _addresses = new HashMap<String, String>();
        _addresses.put("5777", "0xCfEB869F69431e42cdB54A4F4f105C19C080A601");
    }

    @Deprecated
    protected Settlement(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Settlement(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Settlement(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Settlement(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public List<LockedEventResponse> getLockedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(LOCKED_EVENT, transactionReceipt);
        ArrayList<LockedEventResponse> responses = new ArrayList<LockedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            LockedEventResponse typedResponse = new LockedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.settlementId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.swapId = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.encodedSwapDetail = (byte[]) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<LockedEventResponse> lockedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, LockedEventResponse>() {
            @Override
            public LockedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(LOCKED_EVENT, log);
                LockedEventResponse typedResponse = new LockedEventResponse();
                typedResponse.log = log;
                typedResponse.settlementId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.swapId = (String) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.encodedSwapDetail = (byte[]) eventValues.getNonIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<LockedEventResponse> lockedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(LOCKED_EVENT));
        return lockedEventFlowable(filter);
    }

    public List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, OwnershipTransferredEventResponse>() {
            @Override
            public OwnershipTransferredEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
                OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
                typedResponse.log = log;
                typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
        return ownershipTransferredEventFlowable(filter);
    }

    public List<UnlockedEventResponse> getUnlockedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(UNLOCKED_EVENT, transactionReceipt);
        ArrayList<UnlockedEventResponse> responses = new ArrayList<UnlockedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            UnlockedEventResponse typedResponse = new UnlockedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.settlementId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.swapId = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.encodedSwapDetail = (byte[]) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<UnlockedEventResponse> unlockedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, UnlockedEventResponse>() {
            @Override
            public UnlockedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(UNLOCKED_EVENT, log);
                UnlockedEventResponse typedResponse = new UnlockedEventResponse();
                typedResponse.log = log;
                typedResponse.settlementId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.swapId = (String) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.encodedSwapDetail = (byte[]) eventValues.getNonIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<UnlockedEventResponse> unlockedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(UNLOCKED_EVENT));
        return unlockedEventFlowable(filter);
    }

    public RemoteFunctionCall<Boolean> isOwner() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ISOWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<String> owner() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_RENOUNCEOWNERSHIP, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple5<String, String, BigInteger, BigInteger, BigInteger>> swapIdToDetailMap(String param0) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_SWAPIDTODETAILMAP, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint8>() {}));
        return new RemoteFunctionCall<Tuple5<String, String, BigInteger, BigInteger, BigInteger>>(function,
                new Callable<Tuple5<String, String, BigInteger, BigInteger, BigInteger>>() {
                    @Override
                    public Tuple5<String, String, BigInteger, BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple5<String, String, BigInteger, BigInteger, BigInteger>(
                                (String) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue(), 
                                (BigInteger) results.get(4).getValue());
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_TRANSFEROWNERSHIP, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(newOwner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> lock(String _swapId, String _transferFromAddress, String _transferToAddress, BigInteger _weiAmount, BigInteger _securityAmount, BigInteger weiValue) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_LOCK, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(_swapId), 
                new org.web3j.abi.datatypes.Address(_transferFromAddress), 
                new org.web3j.abi.datatypes.Address(_transferToAddress), 
                new org.web3j.abi.datatypes.generated.Uint256(_weiAmount), 
                new org.web3j.abi.datatypes.generated.Uint256(_securityAmount)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> unlock(String _swapId) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_UNLOCK, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(_swapId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static Settlement load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Settlement(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Settlement load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Settlement(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Settlement load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Settlement(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Settlement load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Settlement(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Settlement> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, String _cordaNotary) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_cordaNotary)));
        return deployRemoteCall(Settlement.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<Settlement> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, String _cordaNotary) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_cordaNotary)));
        return deployRemoteCall(Settlement.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Settlement> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String _cordaNotary) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_cordaNotary)));
        return deployRemoteCall(Settlement.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Settlement> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String _cordaNotary) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_cordaNotary)));
        return deployRemoteCall(Settlement.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    protected String getStaticDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }

    public static String getPreviouslyDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }

    public static class LockedEventResponse extends BaseEventResponse {
        public BigInteger settlementId;

        public String swapId;

        public byte[] encodedSwapDetail;
    }

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {
        public String previousOwner;

        public String newOwner;
    }

    public static class UnlockedEventResponse extends BaseEventResponse {
        public BigInteger settlementId;

        public String swapId;

        public byte[] encodedSwapDetail;
    }
}
