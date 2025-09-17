App Usage Tutorial
This tutorial guides you through testing the application's core features using Postman.

Prerequisites
Postman: Ensure Postman is installed.
Services Running: M1 (port 8081), M2 (port 8082), M3 (port 8083) should be running.
Database: Your MySQL database should be running and accessible.
Setup (Postman Environment Recommended)
1. Postman Testing Guide
Authentication & User Setup
Register User 1

Method: POST
URL: http://localhost:8081/api/user/register
Headers: Content-Type: application/json
Body (raw JSON):
{
    "name": "Alice",
    "email": "alice@example.com",
    "password": "password123",
    "roleName": "USER"
}
Response: 201 Created
Register User 2

Method: POST
URL: http://localhost:8081/api/user/register
Headers: Content-Type: application/json
Body (raw JSON):
{
    "name": "Bob",
    "email": "bob@example.com",
    "password": "password456",
    "roleName": "USER"
}
Response: 201 Created
Login (User 1 - Alice)

Method: POST
URL: http://localhost:8081/api/user/login
Headers: Content-Type: application/json
Body (raw JSON):
{
    "email": "alice@example.com",
    "password": "password123"
}
Response: 200 OK. Note the token and userId.
Action: Save the token to the {{authToken}} environment variable. Save Alice's userId to {{userId1}}.
Posts (Requires Authentication for Create/Update/Delete)
Note: For all authenticated requests below, set Authorization: Bearer Token {{authToken}}.
Create Post

Method: POST
URL: http://localhost:8081/api/posts
Authorization: Bearer Token {{authToken}}
Body: form-data
content (Text): My first post! #awesome
postType (Text): TEXT_WITH_IMAGE (Options: TEXT, IMAGE, TEXT_WITH_IMAGE)
hashtags (Text): #awesome (Add more 'hashtags' keys for multiple tags)
image (File): (Optional) Use "Select Files" to upload an image if type includes IMAGE.
Response: 201 Created. Note the id of the new post.
Action: Save the post id to {{postId}}.
Get All Posts (Auth Optional)

Method: GET
URL: http://localhost:8081/api/posts
Authorization: (Optional) Bearer Token {{authToken}}
Response: 200 OK with a list of posts.
Get Posts by User (Auth Optional)

Method: GET
URL: http://localhost:8081/api/posts/user/{{userId1}} (Replace userId1 with desired user ID)
Authorization: (Optional) Bearer Token {{authToken}}
Response: 200 OK with posts by the specified user.
Get Posts by Hashtag (Auth Optional)

Method: GET
URL: http://localhost:8081/api/posts/hashtag/java (if you want to see the posts with #java hashtag)
Authorization: (Optional) Bearer Token {{authToken}}
Response: 200 OK with posts containing the hashtag.
Update Post

Method: PUT
URL: http://localhost:8081/api/posts/{{postId}}
Authorization: Bearer Token {{authToken}}
Body: form-data (Include fields to update, e.g., content, hashtags, image)
content: My updated post content. #updated
postType: TEXT
hashtags: #updated
Response: 200 OK with the updated post details.
Comments (Requires Authentication for Create/Update/Delete)
Note: For all authenticated requests below, set Authorization: Bearer Token {{authToken}}.
Create Comment

Method: POST
URL: http://localhost:8081/api/comments/post/{{postId}} (Use ID of an existing post)
Authorization: Bearer Token {{authToken}}
Headers: Content-Type: application/json
Body (raw JSON):
{
    "content": "This is a comment on the post!"
}
Response: 201 Created. Note the id of the new comment.
Action: Save the comment id to {{commentId}}.
Get Comments for Post (Auth Optional)

Method: GET
URL: http://localhost:8081/api/comments/post/{{postId}}
Authorization: (Optional) Bearer Token {{authToken}}
Response: 200 OK with a list of comments for the post.
Friend Requests (Requires Authentication)
Note: For all authenticated requests below, set Authorization: Bearer Token {{authToken}}. You may need to log in as the appropriate user (sender or receiver) and update {{authToken}}.
Send Friend Request (e.g., Alice sends to Bob)

Method: POST
URL: {{baseURL}}/api/friends/send/{{userId2}} (Use receiver's ID)
Authorization: Bearer Token {{authToken}} (Sender's token)
Response: 200 OK. If the request ID is returned, note it.
Action: Assume request ID is 1 for example, save to {{friendRequestId}}.
Accept Friend Request (e.g., Bob accepts from Alice)

Action: Login as Bob, update {{authToken}} with Bob's token.
Method: POST
URL: {{baseURL}}/api/friends/accept/{{friendRequestId}} (Use the ID of the request)
Authorization: Bearer Token {{authToken}} (Receiver's token)
Response: 200 OK.
Reject Friend Request (Alternative to Accept)

Action: Login as Bob, update {{authToken}} with Bob's token.
Method: POST
URL: {{baseURL}}/api/friends/reject/{{friendRequestId}} (Use the ID of the request)
Authorization: Bearer Token {{authToken}} (Receiver's token)
Response: 200 OK.
View Pending Requests (e.g., Bob views requests sent to him)

Action: Ensure logged in as the user whose pending requests you want to see (e.g., Bob), update {{authToken}}.
Method: GET
URL: {{baseURL}}/api/friends/pending
Authorization: Bearer Token {{authToken}}
Response: 200 OK with a list of pending requests where the current user is the receiver.
User Management (Requires Admin Authentication - Be Cautious with Update/Delete)
Register Admin User

Method: POST
URL: http://localhost:8081/api/user/register
Headers: Content-Type: application/json
Body (raw JSON):
{
"name": "Admin_user3",
"email": "admin3@admin.com",
"password": "adminpassword",
"roleName": "ADMIN"
}
Response: 201 Created.
Login as Admin

Method: POST
URL: http://localhost:8081/api/user/login
Headers: Content-Type: application/json
Body (raw JSON):
{
"email": "admin3@admin.com",
"password": "adminpassword"
}
Response: 200 OK. Note the token and userId.
Block User

Method: POST
URL: http://localhost:8081/api/user/block/{{userId}} (Use the ID of the user to block)
Authorization: Bearer Token {{authToken}} (Admin's token)
Response: 200 OK.
Unblock User

Method: POST
URL: http://localhost:8081/api/user/unblock/{{userId}} (Use the ID of the user to unblock)
Authorization: Bearer Token {{authToken}} (Admin's token)
Response: 200 OK.
Delete a User

Method: DELETE
URL: http://localhost:8081/api/user/{{userId}} (Use the ID of the user to delete)
Authorization: Bearer Token {{authToken}} (Admin's token)
Response: 200 OK.
Note:* Be cautious with this action as it permanently deletes the user and their data.
Delete a Comment

Method: DELETE
URL: http://localhost:8081/api/comments/{{commentId}} (Use the ID of the comment to delete)
Authorization: Bearer Token {{authToken}} (User's token)
Response: 200 OK.
Note:* Be cautious with this action as it permanently deletes the comment.
Testing Workflow Summary
Register User 1 (Alice) and User 2 (Bob).
Login as Alice, save her token to {{authToken}} and ID to {{userId1}}. Save Bob's ID to {{userId2}}.
(As Alice) Create a Post, save ID to {{postId}}.
(As Alice) Create a Comment on the post, save ID to {{commentId}}.
Test various GET endpoints for Posts and Comments (Auth optional).
(As Alice) Update the Post.
(As Alice) Update the Comment.
(As Alice) Send Friend Request to Bob ({{userId2}}), save request ID to {{friendRequestId}}.
Login as Bob, update {{authToken}}.
(As Bob) Accept/Reject Friend Request using {{friendRequestId}}.
Login back as Alice (update {{authToken}}).
(As Alice) Delete the Comment using {{commentId}}.
(As Alice) Delete the Post using {{postId}}.
(As Admin) Register an Admin user, login, and test blocking/unblocking users.
(As Admin) Delete a user using their ID.
(As Admin) Delete a comment using its ID.
Remember to replace placeholders like {postId}, {commentId}, {userId1}, {userId2}, {friendRequestId} and YOUR_JWT_TOKEN (via {{authToken}}) with actual values during testing.
Aplicație tip rețea socială, cu backend distribuit pe microservicii (M1, M2, M3) și bază de date MySQL, care oferă funcționalități precum înregistrare și autentificare utilizatori, gestionarea postărilor (text, imagini, hashtag-uri), comentarii, cereri de prietenie și management de utilizatori (inclusiv roluri de Admin,Moderator).
