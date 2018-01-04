#ifndef UTIL_H
#define UTIL_H

#include <string>
#include <vector>

namespace hyponome {
  namespace util {

    std::string bin2hex(std::vector<unsigned char>);

    std::vector<unsigned char> hex2bin(std::string);

  }
}

#endif