#import "CubivueScannersPlugin.h"
#if __has_include(<cubivue_scanners/cubivue_scanners-Swift.h>)
#import <cubivue_scanners/cubivue_scanners-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "cubivue_scanners-Swift.h"
#endif

@implementation CubivueScannersPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftCubivueScannersPlugin registerWithRegistrar:registrar];
}
@end
