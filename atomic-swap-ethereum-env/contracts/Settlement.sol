pragma solidity >=0.4.21 <0.7.0;
pragma experimental ABIEncoderV2;

import "openzeppelin-solidity/contracts/ownership/Ownable.sol";

contract Settlement is Ownable {
  enum SwapStatus {
    Locked,
    Unlocked,
    Aborted
  }

  struct SwapDetail {
    address transferFromAddress;
    address transferToAddress;
    uint256 weiAmount;
    uint256 securityAmount;
    SwapStatus status;
  }

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

  event Aborted(
    uint256 settlementId,
    string swapId,
    bytes encodedSwapDetail
  );

  mapping (string => SwapDetail) _swapIdToDetailMaps;

  uint256 settlementId;

  constructor() public {
    settlementId = 1;
  }

  function lock(
    string memory _swapId,
    address _transferFromAddress,
    address _transferToAddress,
    uint256 _weiAmount,
    uint256 _securityAmount
  ) public payable {
    require(msg.sender == _transferFromAddress, "msg.sender is not _transferFromAddress");
    require(msg.value == _weiAmount, "msg.value is not equivalent to _weiAmount");

    SwapDetail storage swapDetail = _swapIdToDetailMaps[_swapId];
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
    string memory _swapId,
    address _transferFromAddress,
    address payable _transferToAddress,
    uint256 _weiAmount
  ) public onlyOwner {
    SwapDetail storage swapDetail = _swapIdToDetailMaps[_swapId];

    require(swapDetail.status == SwapStatus.Locked, "swapDetail.status is not Locked");
    require(swapDetail.transferFromAddress == _transferFromAddress, "swapDetail.transferFromAddress is not equal to _transferFromAddress");
    require(swapDetail.transferToAddress == _transferToAddress, "swapDetail.transferToAddress is not equal to _transferToAddress");
    require(swapDetail.weiAmount == _weiAmount, "swapDetail.weiAmount is not equal to _weiAmount");

    // Try sending wei to targetAddress.
    require(_transferToAddress.send(_weiAmount));

    swapDetail.status = SwapStatus.Unlocked;

    bytes memory encodedSwapDetail = abi.encode(swapDetail);

    emit Unlocked(
      settlementId++,
      _swapId,
      encodedSwapDetail
    );
  }

  function abort(
    string memory _swapId,
    address payable _transferFromAddress,
    address _transferToAddress,
    uint256 _weiAmount
  ) public onlyOwner {
    SwapDetail storage swapDetail = _swapIdToDetailMaps[_swapId];

    require(swapDetail.status == SwapStatus.Locked, "swapDetail.status is not Locked");
    require(swapDetail.transferFromAddress == _transferFromAddress, "swapDetail.transferFromAddress is not equal to _transferFromAddress");
    require(swapDetail.transferToAddress == _transferToAddress, "swapDetail.transferToAddress is not equal to _transferToAddress");
    require(swapDetail.weiAmount == _weiAmount, "swapDetail.weiAmount is not equal to _weiAmount");

    // Try sending wei to targetAddress.
    require(_transferFromAddress.send(_weiAmount));

    swapDetail.status = SwapStatus.Aborted;

    bytes memory encodedSwapDetail = abi.encode(swapDetail);

    emit Aborted(
      settlementId++,
      _swapId,
      encodedSwapDetail
    );
  }
}
