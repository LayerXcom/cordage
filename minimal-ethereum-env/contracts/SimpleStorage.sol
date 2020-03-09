pragma solidity >=0.4.21 <0.7.0;


contract SimpleStorage {
  uint256 public data;

  event Set(uint256 x);

  constructor(uint256 initVal) public {
    data = initVal;
  }

  function set(uint256 x) public {
    emit Set(x);
    data = x;
  }

  function get() public view returns (uint256 retVal) {
    return data;
  }
}
