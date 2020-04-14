pragma solidity >=0.4.21 <0.7.0;
pragma experimental ABIEncoderV2;

import "openzeppelin-solidity/contracts/ownership/Ownable.sol";

contract Settlement is Ownable {
  enum SwapStatus {
    Nonexistent,
    Locked,
    Unlocked
  }

  struct SwapDetail {
    address payable transferFromAddress;
    address payable transferToAddress;
    uint256 weiAmount;
    uint256 securityAmount;
    SwapStatus status;
  }

  address cordaNotary;
  mapping(string => SwapDetail) public swapIdToDetailMap;
  uint256 settlementId;

  event Locked(
    uint256 settlementId,
    string swapId,
    bytes encodedSwapDetail
  );

  event Unlocked(
    uint256 settlementId,
    string swapId,
    bytes encodedSwapDetail
  );

  constructor(address _cordaNotary) public {
    cordaNotary = _cordaNotary;
    settlementId = 1;
  }

  modifier onlyCordaNotary {
    require(msg.sender == cordaNotary, "caller is not the corda notary");
    _;
  }

  function lock(
    string memory _swapId,
    address payable _transferFromAddress,
    address payable _transferToAddress,
    uint256 _weiAmount,
    uint256 _securityAmount
  ) public payable {
    require(msg.sender == _transferFromAddress, "msg.sender is not _transferFromAddress");
    require(msg.value == _weiAmount, "msg.value is not equivalent to _weiAmount");

    SwapDetail storage swapDetail = swapIdToDetailMap[_swapId];
    swapDetail.transferFromAddress = _transferFromAddress;
    swapDetail.transferToAddress = _transferToAddress;
    swapDetail.weiAmount = _weiAmount;
    swapDetail.securityAmount = _securityAmount;
    swapDetail.status = SwapStatus.Locked;

    bytes memory encodedSwapDetail = abi.encode(swapDetail);

    emit Locked(
      settlementId++,
      _swapId,
      encodedSwapDetail
    );
  }

  function unlock(
    string memory _swapId
  ) public onlyCordaNotary {
    SwapDetail storage swapDetail = swapIdToDetailMap[_swapId];

    require(swapDetail.status != SwapStatus.Nonexistent, "swapDetail does not exist");
    require(swapDetail.status == SwapStatus.Locked, "swapDetail.status is not Locked");

    // Try sending wei to targetAddress.
    require(swapDetail.transferToAddress.send(swapDetail.weiAmount));

    swapDetail.status = SwapStatus.Unlocked;

    bytes memory encodedSwapDetail = abi.encode(swapDetail);

    emit Unlocked(
      settlementId++,
      _swapId,
      encodedSwapDetail
    );
  }
}
