![Images](https://github.com/BabatundeOnabajo/Blue-Plaques-of-London-In-Augmented-Reality-AR-Vibe-Coded/blob/main/1769582657178.png)

# Blue-Plaques-of-London-In-Augmented-Reality-AR-Vibe-Coded
This is a mobile phone game that users can play in London. It involves hovering your phone over a blue plaque on a building organised by the English Heritage charity and you will be asked a number of questions in a quiz about the person commemorated by the plaque. 

# Technical Background
The app was built partly (although not entirely, see below under the section "Extent of assistance with 'vibe coding'" for further information) with "vibe coding" which describes the creation of programs through Large Language Models (LLMs) and natural human language. This was done through the following platforms (in order of most used to least used):
1. Amazon Bedrock - Claude Opus 4.1
2. Amazon Bedrock - Claude Opus 4.5
3. ChatGPT

Three LLM platforms were used rather than the more usual one because the augmented reality toolkit provided by Google, called ARCore, is not well-documented and has inconsistent documentation. Furthermore, some platforms outperformed certain aspects of the app development process than others. That it took three LLM platforms to create this app rather than just one highlights the complexity of the Android app development process.

# App Structure
This app is made up of around 32 files or so. The main file is BluePlaqueARActivity.java, which governs the main functionality of the app. 

# Instructions
There are two ways to install this app on your Android device. Please note that the .apk file is not available in this repository, and has been given directly to those it is relevant to. In that case, please follow the second subheading.

## If you have the .apk file
This will likely apply to those who are judges in the New Media Writing Prize. Please note that the ability to sideload apps is, according to reports, gradually being removed by Google. In that case, ignore this. The .apk file is called app-release.apk. 

On an Android device, do the following:
1. Open Settings.
2. Press "Apps".
3. Press the three dots at the top of the screen and press "Special Access".
4. Press "Install unknown apps".
5. Enable "My Files" to install unknown apps.
6. For security reasons, once you have used the app, you should turn it off. You can do that by following these instructions again from 1-4 and then at 5 press "Deny all".
7. Go to the location in My Files where the .apk file is. This will likely be in the "Downloads" folder but may be elsewhere. Press it and this will install the app on your phone.
8. Once the app is installed, voila! You can now detect blue plaques with your phone.

## If you do not have the .apk file
If you do not have the .apk file, you will have to try to reconstruct this project. If you have an Integrated Development Environment (IDE) like Android Studio, this will involve carefully reconstructing this repository onto your device. 

# Advanced Settings
This area is for those who understand the Android development process and want to configure this app so that the app generates images with the help of AWS Bedrock or OpenAI. By default, this app has no access to AWS Bedrock or OpenAI. To enable this, follow the required instructions.

## OpenAI
Obtain your API key from OpenAI and head to gradle.properties. Insert your API key where OpenAI is. This will enable you to use OpenAI's AI features.

## AWS Bedrock
Obtain your API key and access key from AWS Bedrock. Uncomment out all the areas that are coded as commented out in AWSBedrockImageService.java. 

In either case, always keep your keys secure and private. Treat your keys as confidential, so as to prevent people from incurring a bill on your behalf. 

# Limitations
There are some limitations with this app. The first is that in order to use the app in real life, you need to stand very closely to the Blue Plaque otherwise the app will either not detect the Blue Plaque or it will return "Unknown". As a guide, to use this app effectively, you need to ensure that the Blue Plaque is within one half of your arm's distance, and on the same plane as your chest. Relatedly, there is no option to zoom in or out in this app (see below for "Potential room for improvement"). 

# Potential room for improvement
There is some room for improvement with this app. The first is that as of January 29th 2026, this app only has an Android version available. There is a folder in this Github for iOS but that has not been released as of this date. In the context of the Android app, the app should have the ability to zoom in. As of today's date, which is January 29th 2026, there is no ability to do this. 

Another improvement is cosmetic: the app icon currently is the default Android icon. Should a future update be warranted, then a "blue plaque" icon may be created.

Another improvement is that of machine learning. Currently, the app detects blue plaques in a relatively simple manner. It doesn't use any kind of sophisticated machine learning, like that provided in the library TensorFlow. In the future, utilising image recognition from a library like Tensorflow would be a significant improvement and would improve the utility of this app.

# Troubleshooting
For troubleshooting, be sure to contact the author. 

# Blue Plaque Examples
For those not in London or confined to indoors, you can trial out the app at home. Let's use the example of the 20th century economist John Maynard Keynes. He has a Blue Plaque and this is commemorated on the [website of English Heritage](https://www.english-heritage.org.uk/visit/blue-plaques/john-maynard-keynes/). A screenshot of their website is also shown below. Be sure to hover your phone and you should see something like the screenshot underneath!

![Images](https://github.com/BabatundeOnabajo/Blue-Plaques-of-London-In-Augmented-Reality-AR-Vibe-Coded/blob/main/Screenshot%202026-01-28%20at%2015.35.31.png)
![Images](https://github.com/BabatundeOnabajo/Blue-Plaques-of-London-In-Augmented-Reality-AR-Vibe-Coded/blob/main/Screenshot_20260128_110502_Blue%20Plaques%20of%20London%20in%20AR.jpg)

# Sustainable Development Goals (SDGs)
This app conforms to one of the Sustainable Development Goals (SDGs), in this case SDG 11 which is "Sustainable Cities and Communities" and, specifically, 11.4 which reads: "Protect the world's cultural and natural heritage". 

# Extent of assistance with "vibe coding"
This app utilised "vibe coding", which is a new way of software development that involves utilising LLMs to help build software. There are many valid criticisms of this form of software development. In this particular case, vibe coding helped speed up the process of creating this app: what might have ordinarily taken a month or so to have created to get a barebones "skeleton" took around several days. However, despite this, it still involved multiple uses of LLMs, and from different providers, mainly because of the documentation provided by Google and its facility ARCore not being well documented. Although vibe coding is sometimes seen as "amateurish", in this particular case it still required a very advanced knowledge of Android app development and its corresponding Integrated Development Environment (IDE) which is Android Studio. One interesting tidbit is that some of the information provided by the LLM was wrong. For example, it recommended using a List with the generic type argument list "string", when in fact it should be "String" (capitalised "S"). It seems the LLM was confused between C# and Java, and this highlights why prior knowledge of programming is essential when wanting to "vibe code". A screenshot of this is provided below:

![Images](https://github.com/BabatundeOnabajo/Blue-Plaques-of-London-In-Augmented-Reality-AR-Vibe-Coded/blob/main/Screenshot%202026-01-27%20at%2014.53.58.png)

This documentation has been written entirely by a human.
