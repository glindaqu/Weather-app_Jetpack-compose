## Weather app (Jetpack compose)

# Descriptions
This is just small training application. General purpose of this project is know how works retrofit, how get data from the remote server and handle result on client side. In this task been developed android application (android-target only), that's can display weather in 24 hours, detects user location and pick location manually.

# General topics
* Jetpack compose
* MVVM (AndroidViewModel)
* Retrofit 2
* Call interface implementation
* Lottie-compose animations
* Coil (async image loading)
* Open Weather Map API

# Conclusion
Task is done. The difficult been in implement onSuccess and onFailure methods in retrofit service interface. Information about it is so cut (or i'm just don't know how to search it right way). The problem is in the general case we have suspend function in the interface and it returns parsed data-class (just some kind of json object). But there is no way how to detect server error and somehow handle it is impossible in this case. Solution is just use Call<T> as return type of all service fuctions (also it needs to remove suspend modifier, because call is some kind of suspend. It just needs callbacks. But there is second problem: synchronize queries and handle it one by one. My solution of tjis problem is just call second query after first ends successfuly.

# Links 
- https://habr.com/ru/companies/oleg-bunin/articles/543386/
- https://habr.com/ru/companies/oleg-bunin/articles/545702/
- https://futurestud.io/tutorials/retrofit-2-introduction-to-call-adapters
