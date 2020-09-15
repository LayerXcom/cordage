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
    public static final String BINARY = "0x60806040523480156200001157600080fd5b506040516200190f3803806200190f83398181016040526200003791908101906200015a565b336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16600073ffffffffffffffffffffffffffffffffffffffff167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e060405160405180910390a380600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550600160038190555050620001d4565b6000815190506200015481620001ba565b92915050565b6000602082840312156200016d57600080fd5b60006200017d8482850162000143565b91505092915050565b600062000193826200019a565b9050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b620001c58162000186565b8114620001d157600080fd5b50565b61172b80620001e46000396000f3fe60806040526004361061007b5760003560e01c80638f32d59b1161004e5780638f32d59b1461012c578063a96ce7aa14610157578063b7c9587214610180578063f2fde38b1461019c5761007b565b806335acac4e14610080578063534b7fcd146100a9578063715018a6146100ea5780638da5cb5b14610101575b600080fd5b34801561008c57600080fd5b506100a760048036036100a29190810190610dcc565b6101c5565b005b3480156100b557600080fd5b506100d060048036036100cb9190810190610d8b565b61044c565b6040516100e19594939291906112bc565b60405180910390f35b3480156100f657600080fd5b506100ff6104e5565b005b34801561010d57600080fd5b506101166105eb565b60405161012391906112a1565b60405180910390f35b34801561013857600080fd5b50610141610614565b60405161014e919061130f565b60405180910390f35b34801561016357600080fd5b5061017e60048036036101799190810190610dcc565b61066b565b005b61019a60048036036101959190810190610e0d565b6108f2565b005b3480156101a857600080fd5b506101c360048036036101be9190810190610d62565b610afa565b005b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614610255576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161024c9061134a565b60405180910390fd5b6000600282604051610267919061128a565b908152602001604051809103902090506000600381111561028457fe5b8160040160009054906101000a900460ff1660038111156102a157fe5b14156102e2576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016102d9906113ea565b60405180910390fd5b600160038111156102ef57fe5b8160040160009054906101000a900460ff16600381111561030c57fe5b1461034c576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610343906113ca565b60405180910390fd5b8060000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc82600201549081150290604051600060405180830381858888f193505050506103b257600080fd5b60038160040160006101000a81548160ff021916908360038111156103d357fe5b02179055506060816040516020016103eb919061140a565b60405160208183030381529060405290507f8aa49386b7575bc2e29336acb6994ab3190495f485fc19950577915bd153cdd56003600081548092919060010191905055848360405161043f93929190611425565b60405180910390a1505050565b6002818051602081018201805184825260208301602085012081835280955050505050506000915090508060000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16908060010160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16908060020154908060030154908060040160009054906101000a900460ff16905085565b6104ed610614565b61052c576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610523906113aa565b60405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff166000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e060405160405180910390a360008060006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905090565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614905090565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16146106fb576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016106f29061134a565b60405180910390fd5b600060028260405161070d919061128a565b908152602001604051809103902090506000600381111561072a57fe5b8160040160009054906101000a900460ff16600381111561074757fe5b1415610788576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161077f906113ea565b60405180910390fd5b6001600381111561079557fe5b8160040160009054906101000a900460ff1660038111156107b257fe5b146107f2576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016107e9906113ca565b60405180910390fd5b8060010160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc82600201549081150290604051600060405180830381858888f1935050505061085857600080fd5b60028160040160006101000a81548160ff0219169083600381111561087957fe5b0217905550606081604051602001610891919061140a565b60405160208183030381529060405290507f7e646be22d23cac996ea886cf1f7bc6b43310119eeee9c8d65cf8e8008af9926600360008154809291906001019190505584836040516108e593929190611425565b60405180910390a1505050565b8373ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614610960576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016109579061138a565b60405180910390fd5b8134146109a2576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016109999061136a565b60405180910390fd5b60006002866040516109b4919061128a565b90815260200160405180910390209050848160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550838160010160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555082816002018190555081816003018190555060018160040160006101000a81548160ff02191690836003811115610a7d57fe5b0217905550606081604051602001610a95919061140a565b60405160208183030381529060405290507fe7e61de6a45eb1bdc4b022cd7eaf8344da1283e4531bcbe37baeba6cbf65247660036000815480929190600101919050558883604051610ae993929190611425565b60405180910390a150505050505050565b610b02610614565b610b41576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610b38906113aa565b60405180910390fd5b610b4a81610b4d565b50565b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff161415610bbd576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610bb49061132a565b60405180910390fd5b8073ffffffffffffffffffffffffffffffffffffffff166000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff167f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e060405160405180910390a3806000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555050565b600081359050610c8a816116a3565b92915050565b600081359050610c9f816116ba565b92915050565b600082601f830112610cb657600080fd5b8135610cc9610cc482611497565b61146a565b91508082526020830160208301858383011115610ce557600080fd5b610cf08382846115e8565b50505092915050565b600082601f830112610d0a57600080fd5b8135610d1d610d18826114c3565b61146a565b91508082526020830160208301858383011115610d3957600080fd5b610d448382846115e8565b50505092915050565b600081359050610d5c816116d1565b92915050565b600060208284031215610d7457600080fd5b6000610d8284828501610c7b565b91505092915050565b600060208284031215610d9d57600080fd5b600082013567ffffffffffffffff811115610db757600080fd5b610dc384828501610ca5565b91505092915050565b600060208284031215610dde57600080fd5b600082013567ffffffffffffffff811115610df857600080fd5b610e0484828501610cf9565b91505092915050565b600080600080600060a08688031215610e2557600080fd5b600086013567ffffffffffffffff811115610e3f57600080fd5b610e4b88828901610cf9565b9550506020610e5c88828901610c90565b9450506040610e6d88828901610c90565b9350506060610e7e88828901610d4d565b9250506080610e8f88828901610d4d565b9150509295509295909350565b610ea58161157b565b82525050565b610eb48161157b565b82525050565b610ec381611569565b82525050565b610ed28161158d565b82525050565b6000610ee3826114ef565b610eed8185611505565b9350610efd8185602086016115f7565b610f0681611678565b840191505092915050565b610f1a816115d6565b82525050565b610f29816115d6565b82525050565b6000610f3a826114fa565b610f448185611516565b9350610f548185602086016115f7565b610f5d81611678565b840191505092915050565b6000610f73826114fa565b610f7d8185611527565b9350610f8d8185602086016115f7565b80840191505092915050565b6000610fa6602683611516565b91507f4f776e61626c653a206e6577206f776e657220697320746865207a65726f206160008301527f64647265737300000000000000000000000000000000000000000000000000006020830152604082019050919050565b600061100c601e83611516565b91507f63616c6c6572206973206e6f742074686520636f726461206e6f7461727900006000830152602082019050919050565b600061104c602983611516565b91507f6d73672e76616c7565206973206e6f74206571756976616c656e7420746f205f60008301527f776569416d6f756e7400000000000000000000000000000000000000000000006020830152604082019050919050565b60006110b2602683611516565b91507f6d73672e73656e646572206973206e6f74205f7472616e7366657246726f6d4160008301527f64647265737300000000000000000000000000000000000000000000000000006020830152604082019050919050565b6000611118602083611516565b91507f4f776e61626c653a2063616c6c6572206973206e6f7420746865206f776e65726000830152602082019050919050565b6000611158601f83611516565b91507f7377617044657461696c2e737461747573206973206e6f74204c6f636b6564006000830152602082019050919050565b6000611198601983611516565b91507f7377617044657461696c20646f6573206e6f74206578697374000000000000006000830152602082019050919050565b60a0820160008083015490506111e08161162a565b6111ed6000860182610e9c565b50600183015490506111fe8161162a565b61120b6020860182610e9c565b506002830154905061121c8161165e565b611229604086018261126c565b506003830154905061123a8161165e565b611247606086018261126c565b506004830154905061125881611644565b6112656080860182610f11565b5050505050565b611275816115cc565b82525050565b611284816115cc565b82525050565b60006112968284610f68565b915081905092915050565b60006020820190506112b66000830184610eba565b92915050565b600060a0820190506112d16000830188610eab565b6112de6020830187610eab565b6112eb604083018661127b565b6112f8606083018561127b565b6113056080830184610f20565b9695505050505050565b60006020820190506113246000830184610ec9565b92915050565b6000602082019050818103600083015261134381610f99565b9050919050565b6000602082019050818103600083015261136381610fff565b9050919050565b600060208201905081810360008301526113838161103f565b9050919050565b600060208201905081810360008301526113a3816110a5565b9050919050565b600060208201905081810360008301526113c38161110b565b9050919050565b600060208201905081810360008301526113e38161114b565b9050919050565b600060208201905081810360008301526114038161118b565b9050919050565b600060a08201905061141f60008301846111cb565b92915050565b600060608201905061143a600083018661127b565b818103602083015261144c8185610f2f565b905081810360408301526114608184610ed8565b9050949350505050565b6000604051905081810181811067ffffffffffffffff8211171561148d57600080fd5b8060405250919050565b600067ffffffffffffffff8211156114ae57600080fd5b601f19601f8301169050602081019050919050565b600067ffffffffffffffff8211156114da57600080fd5b601f19601f8301169050602081019050919050565b600081519050919050565b600081519050919050565b600082825260208201905092915050565b600082825260208201905092915050565b600081905092915050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b600060ff82169050919050565b6000819050919050565b6000611574826115ac565b9050919050565b6000611586826115ac565b9050919050565b60008115159050919050565b60008190506115a782611696565b919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b60006115e182611599565b9050919050565b82818337600083830152505050565b60005b838110156116155780820151818401526020810190506115fa565b83811115611624576000848401525b50505050565b600061163d61163883611689565b611532565b9050919050565b600061165761165283611689565b611552565b9050919050565b600061167161166c83611689565b61155f565b9050919050565b6000601f19601f8301169050919050565b60008160001c9050919050565b600481106116a057fe5b50565b6116ac81611569565b81146116b757600080fd5b50565b6116c38161157b565b81146116ce57600080fd5b50565b6116da816115cc565b81146116e557600080fd5b5056fea365627a7a72315820f1350871d9dec3571f8cc37c45d7dcd1e2778248dd8d4c29dfc1e007ddeac0b76c6578706572696d656e74616cf564736f6c63430005110040";

    public static final String FUNC_ISOWNER = "isOwner";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_SWAPIDTODETAILMAP = "swapIdToDetailMap";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final String FUNC_LOCK = "lock";

    public static final String FUNC_UNLOCK = "unlock";

    public static final String FUNC_ABORT = "abort";

    public static final Event ABORTED_EVENT = new Event("Aborted", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<DynamicBytes>() {}));
    ;

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

    public List<AbortedEventResponse> getAbortedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(ABORTED_EVENT, transactionReceipt);
        ArrayList<AbortedEventResponse> responses = new ArrayList<AbortedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            AbortedEventResponse typedResponse = new AbortedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.settlementId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.swapId = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.encodedSwapDetail = (byte[]) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<AbortedEventResponse> abortedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, AbortedEventResponse>() {
            @Override
            public AbortedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(ABORTED_EVENT, log);
                AbortedEventResponse typedResponse = new AbortedEventResponse();
                typedResponse.log = log;
                typedResponse.settlementId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.swapId = (String) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.encodedSwapDetail = (byte[]) eventValues.getNonIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<AbortedEventResponse> abortedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ABORTED_EVENT));
        return abortedEventFlowable(filter);
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

    public RemoteFunctionCall<TransactionReceipt> abort(String _swapId) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_ABORT, 
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

    public static class AbortedEventResponse extends BaseEventResponse {
        public BigInteger settlementId;

        public String swapId;

        public byte[] encodedSwapDetail;
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
