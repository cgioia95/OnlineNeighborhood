## OnlineNeighborhood

#### What is OnlineNeighborhood:

OnlineNeighborhood came out of a Unversity Project to devise a completely new app idea and fully implement it as a team. 

The core idea is to allow users to post events publically or privately, adding attributes to them that may be of interest (e.g. a sporting event, study session, etc). 

Users can then hop on the app and filter events in an easy-to-use list, or visualise them on a map. The map helps for users looking to attend events more local to them, and eventually foster a sense of community through the app.

![1](https://user-images.githubusercontent.com/38366698/143181004-d13aa3c7-39cf-43ef-bdfa-9164f52a0b73.png)
![2](https://user-images.githubusercontent.com/38366698/143181007-550c0659-6c2d-4d3c-8d27-973d9ca2bd5d.png)
![3](https://user-images.githubusercontent.com/38366698/143181012-88c0cfc7-bb4b-4ab2-9a82-00a2a2c31e15.png)
![4](https://user-images.githubusercontent.com/38366698/143181021-dc5d8b26-a8c2-466a-992d-ea944fcfe26a.png)

## Installation and Setup Instructions

Locally, the application is easy to run in [Android Studio](https://developer.android.com/studio).

Once in Android Studio, follow [these](https://www.tvisha.com/blog/how-to-import-a-project-in-android-studio) steps to import, run and export for deployment usage. 

The backend is driven by Firebase Auth & Realtime Database. Once these two services are manually created, their respective details must be entered into `/app/google-services.json`. 

NOTE: Currently listed config information is deprecated and all services associated with them are shutdown. They are just there for reference of what end-state should look like.

## Reflection

OnlineNeighborhood was a fantastic learning opportunity that fostered invaluable skills I take with me today. Firstly, working on a relatively complex project as a team required strong communication and planning. I learnt the value of segmenting work correctly, and delegating to the respective strengths of team members. 

More technically, this was my first time working working in App Development and I'm now confident in my ability to produce even better applications. 

The backend (Authentication & Database) still requires manual creation, so a future enhancement would be implementing a Infrastructure as Code (IaC). 
Additionally, a mock server would be helpful in assisting faster local development, as it currently requires data in the development server to test. 
