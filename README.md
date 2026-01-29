# Blue-Plaques-of-London-In-Augmented-Reality-AR-Vibe-Coded
This is a mobile phone game that users can play in London. It involves hovering your phone over a blue plaque on a building organised by the English Heritage charity and you will be asked a number of questions in a quiz about the person commemorated by the plaque. 

# Technical Background
The app was built partly (although not entirely, see below under the section "Extent of assistance with 'vibe coding'" for further information) with "vibe coding" which describes the creation of programs through Large Language Models (LLMs) and natural human language. This was done through the following platforms (in order of most used to least used):
1. Amazon Bedrock - Claude Opus 4.1
2. Amazon Bedrock - Claude Opus 4.5
3. ChatGPT

Three LLM platforms were used rather than the more usual one because the augmented reality toolkit provided by Google, called ARCore, is not well-documented and has inconsistent documentation. Furthermore, some platforms outperformed certain aspects of the app development process than others. That it took three LLM platforms to create this app rather than just one highlights the complexity of the Android app development process.

# App Structure

# Instructions

# Advanced Settings
This area is for those who understand the Android development process and want to configure this app so that the app generates images with the help of AWS Bedrock or OpenAI.

# Limitations
There are some limitations with this app. The first is that in order to use the app in real life, you need to stand very closely to the Blue Plaque otherwise the app will either not detect the Blue Plaque or it will return "Unknown". As a guide, to use this app effectively, you need to ensure that the Blue Plaque is within one half of your arm's distance, and on the same plane as your chest. Relatedly, there is no option to zoom in or out in this app (see below for "Potential room for improvement"). 

# Potential room for improvement

# Troubleshooting

# Blue Plaque Examples
For those not in London or confined to indoors, you can trial out the app at home. Let's use the example of the 20th century economist John Maynard Keynes. He has a Blue Plaque and this is commemorated on the [website of English Heritage](https://www.english-heritage.org.uk/visit/blue-plaques/john-maynard-keynes/).

# Extent of assistance with "vibe coding"
