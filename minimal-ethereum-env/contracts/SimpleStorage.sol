pragma solidity >=0.4.21 <0.7.0;

contract SimpleStorage {
  uint public data;

  event Set (uint x);

  constructor(uint initVal) public {
    data = initVal;
  }

  function set(uint x) public {
    emit Set(x);
    data = x;
  }

  function get() public view returns (uint retVal) {
    return data;
  }
}
