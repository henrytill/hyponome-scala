add_library(Hyponome
  hash.cpp
  rpc.cpp
  util.cpp)

target_link_libraries(Hyponome
  CapnProtoDefs
  ${CAPNP_LIBRARIES}
  ${SODIUM_LIBRARY})
