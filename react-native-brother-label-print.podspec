require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = package["name"]
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  React Native module for printing with Brother printers via WiFi and Bluetooth
                  DESC
  s.homepage     = package["repository"]["baseUrl"]
  s.license      = package["license"]
  s.authors      = { package["author"]["name"] => package["author"]["email"] }
  s.platforms    = { :ios => "12.0" }
  s.source       = { :git => package["repository"]["url"], :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,c,cc,cpp,m,mm,swift,plist}"
  s.requires_arc = true
  s.resources = 'ios/**/*.plist'

  s.dependency "React"
  s.dependency "BRLMPrinterKitSDK"
end 