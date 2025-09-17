# App Usage Tutorial

Acest tutorial te ghidează prin testarea funcționalităților cheie ale aplicației folosind Postman.


## ⚙️ Cerințe preliminare

* **Postman**: Asigură-te că Postman este instalat.
* **Servicii Rulând**: M1 (port `8081`), M2 (port `8082`), M3 (port `8083`) trebuie să fie pornite.
* **Bază de Date**: Baza ta de date MySQL trebuie să ruleze și să fie accesibilă.

---

## 🔧 Configurare (Recomandat: Postman Environment)

1.  ### Ghid de Testare Postman

    #### Autentificare & Configurarea Utilizatorilor

    ---

    ##### **1. Înregistrare Utilizator 1 (Alice)**
    ```
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
    ```
    **Response:** `201 Created`

    ##### **2. Înregistrare Utilizator 2 (Bob)**
    ```
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
    ```
    **Response:** `201 Created`

    ---

    ##### **3. Autentificare (Utilizator 1 - Alice)**
    ```
    Method: POST
    URL: http://localhost:8081/api/user/login
    Headers: Content-Type: application/json
    Body (raw JSON):
    {
        "email": "alice@example.com",
        "password": "password123"
    }
    ```
    **Response:** `200 OK`. Notează `token`-ul și `userId`-ul.
    **Acțiune:** Salvează `token`-ul în variabila de mediu `{{authToken}}`. Salvează `userId`-ul lui Alice în `{{userId1}}`.

---

### **Funcționalități Principale**

#### Postări (Necesită Autentificare pentru Creare/Actualizare/Ștergere)

**Notă:** Pentru toate cererile autentificate de mai jos, setează **Authorization: Bearer Token** `{{authToken}}`.

---

* **Creare Postare**
    ```
    Method: POST
    URL: http://localhost:8081/api/posts
    Authorization: Bearer Token {{authToken}}
    Body: form-data
        content (Text): My first post! #awesome
        postType (Text): TEXT_WITH_IMAGE (Opțiuni: TEXT, IMAGE, TEXT_WITH_IMAGE)
        hashtags (Text): #awesome (Adaugă mai multe chei 'hashtags' pentru etichete multiple)
        image (File): (Opțional) Folosește "Select Files" pentru a încărca o imagine dacă tipul include IMAGE.
    ```
    **Response:** `201 Created`. Notează `id`-ul noii postări.
    **Acțiune:** Salvează `id`-ul postării în variabila de mediu `{{postId}}`.

* **Obține Toate Postările** (Autentificare Opțională)
    ```
    Method: GET
    URL: http://localhost:8081/api/posts
    Authorization: (Opțional) Bearer Token {{authToken}}
    ```
    **Response:** `200 OK` cu o listă de postări.

* **Obține Postări după Utilizator** (Autentificare Opțională)
    ```
    Method: GET
    URL: http://localhost:8081/api/posts/user/{{userId1}} (Înlocuiește userId1 cu ID-ul dorit)
    Authorization: (Opțional) Bearer Token {{authToken}}
    ```
    **Response:** `200 OK` cu postările utilizatorului specificat.

* **Obține Postări după Hashtag** (Autentificare Opțională)
    ```
    Method: GET
    URL: http://localhost:8081/api/posts/hashtag/java
    Authorization: (Opțional) Bearer Token {{authToken}}
    ```
    **Response:** `200 OK` cu postările care conțin hashtag-ul.

* **Actualizare Postare**
    ```
    Method: PUT
    URL: http://localhost:8081/api/posts/{{postId}}
    Authorization: Bearer Token {{authToken}}
    Body: form-data (Include câmpuri de actualizat, ex: content, hashtags, image)
        content: My updated post content. #updated
        postType: TEXT
        hashtags: #updated
    ```
    **Response:** `200 OK` cu detaliile postării actualizate.

---

#### Comentarii (Necesită Autentificare pentru Creare/Actualizare/Ștergere)

**Notă:** Pentru toate cererile autentificate de mai jos, setează **Authorization: Bearer Token** `{{authToken}}`.

---

* **Creare Comentariu**
    ```
    Method: POST
    URL: http://localhost:8081/api/comments/post/{{postId}} (Folosește ID-ul unei postări existente)
    Authorization: Bearer Token {{authToken}}
    Headers: Content-Type: application/json
    Body (raw JSON):
    {
        "content": "This is a comment on the post!"
    }
    ```
    **Response:** `201 Created`. Notează `id`-ul noului comentariu.
    **Acțiune:** Salvează `id`-ul comentariului în variabila de mediu `{{commentId}}`.

* **Obține Comentarii pentru o Postare** (Autentificare Opțională)
    ```
    Method: GET
    URL: http://localhost:8081/api/comments/post/{{postId}}
    Authorization: (Opțional) Bearer Token {{authToken}}
    ```
    **Response:** `200 OK` cu o listă de comentarii pentru postare.

---

#### Cereri de Prietenie (Necesită Autentificare)

**Notă:** Pentru toate cererile autentificate de mai jos, setează **Authorization: Bearer Token** `{{authToken}}`. S-ar putea să fie nevoie să te autentifici ca utilizatorul corespunzător (expeditor sau destinatar) și să actualizezi `{{authToken}}`.

---

* **Trimite Cerere de Prietenie** (ex: Alice trimite lui Bob)
    ```
    Method: POST
    URL: {{baseURL}}/api/friends/send/{{userId2}} (Folosește ID-ul destinatarului)
    Authorization: Bearer Token {{authToken}} (Token-ul expeditorului)
    ```
    **Response:** `200 OK`. Dacă este returnat ID-ul cererii, notează-l.
    **Acțiune:** Presupunem că ID-ul cererii este `1`, salvează-l în `{{friendRequestId}}`.

* **Acceptă Cerere de Prietenie** (ex: Bob acceptă de la Alice)
    **Acțiune:** Autentifică-te ca Bob, actualizează `{{authToken}}` cu token-ul lui Bob.
    ```
    Method: POST
    URL: {{baseURL}}/api/friends/accept/{{friendRequestId}} (Folosește ID-ul cererii)
    Authorization: Bearer Token {{authToken}} (Token-ul destinatarului)
    ```
    **Response:** `200 OK`.

* **Respinge Cerere de Prietenie** (Alternativă la Acceptare)
    **Acțiune:** Autentifică-te ca Bob, actualizează `{{authToken}}` cu token-ul lui Bob.
    ```
    Method: POST
    URL: {{baseURL}}/api/friends/reject/{{friendRequestId}} (Folosește ID-ul cererii)
    Authorization: Bearer Token {{authToken}} (Token-ul destinatarului)
    ```
    **Response:** `200 OK`.

* **Vizualizează Cereri în Așteptare** (ex: Bob vizualizează cererile trimise către el)
    **Acțiune:** Asigură-te că ești autentificat ca utilizatorul ale cărui cereri în așteptare vrei să le vezi (ex: Bob), actualizează `{{authToken}}`.
    ```
    Method: GET
    URL: {{baseURL}}/api/friends/pending
    Authorization: Bearer Token {{authToken}}
    ```
    **Response:** `200 OK` cu o listă de cereri în așteptare unde utilizatorul curent este destinatarul.

---

#### Administrare Utilizatori (Necesită Autentificare Admin - Fii precaut cu Actualizarea/Ștergerea)

---

* **Înregistrare Utilizator Admin**
    ```
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
    ```
    **Response:** `201 Created`.

* **Autentificare ca Admin**
    ```
    Method: POST
    URL: http://localhost:8081/api/user/login
    Headers: Content-Type: application/json
    Body (raw JSON):
    {
        "email": "admin3@admin.com",
        "password": "adminpassword"
    }
    ```
    **Response:** `200 OK`. Notează `token`-ul și `userId`-ul.

* **Blocare Utilizator**
    ```
    Method: POST
    URL: http://localhost:8081/api/user/block/{{userId}} (Folosește ID-ul utilizatorului de blocat)
    Authorization: Bearer Token {{authToken}} (Token-ul Adminului)
    ```
    **Response:** `200 OK`.

* **Deblocare Utilizator**
    ```
    Method: POST
    URL: http://localhost:8081/api/user/unblock/{{userId}} (Folosește ID-ul utilizatorului de deblocat)
    Authorization: Bearer Token {{authToken}} (Token-ul Adminului)
    ```
    **Response:** `200 OK`.

* **Ștergere Utilizator**
    ```
    Method: DELETE
    URL: http://localhost:8081/api/user/{{userId}} (Folosește ID-ul utilizatorului de șters)
    Authorization: Bearer Token {{authToken}} (Token-ul Adminului)
    ```
    **Response:** `200 OK`.
    **Notă:** Fii precaut, această acțiune șterge permanent utilizatorul și datele sale.

* **Ștergere Comentariu**
    ```
    Method: DELETE
    URL: http://localhost:8081/api/comments/{{commentId}} (Folosește ID-ul comentariului de șters)
    Authorization: Bearer Token {{authToken}} (Token-ul utilizatorului)
    ```
    **Response:** `200 OK`.
    **Notă:** Fii precaut, această acțiune șterge permanent comentariul.

---

### 📝 Sumarul Fluxului de Testare

1.  **Înregistrează** Utilizatorul 1 (**Alice**) și Utilizatorul 2 (**Bob**).
2.  **Autentifică-te** ca Alice, salvează `token`-ul în `{{authToken}}` și ID-ul în `{{userId1}}`. Salvează ID-ul lui Bob în `{{userId2}}`.
3.  **(Ca Alice)** **Creează** o Postare, salvează ID-ul în `{{postId}}`.
4.  **(Ca Alice)** **Creează** un Comentariu pe postare, salvează ID-ul în `{{commentId}}`.
5.  **Testează** diverse endpoint-uri `GET` pentru Postări și Comentarii (Autentificare opțională).
6.  **(Ca Alice)** **Actualizează** Postarea.
7.  **(Ca Alice)** **Actualizează** Comentariul.
8.  **(Ca Alice)** **Trimite** o Cerere de Prietenie către Bob (`{{userId2}}`), salvează ID-ul cererii în `{{friendRequestId}}`.
9.  **Autentifică-te** ca Bob, actualizează `{{authToken}}`.
10. **(Ca Bob)** **Acceptă/Respinge** Cererea de Prietenie folosind `{{friendRequestId}}`.
11. **Autentifică-te** din nou ca Alice (actualizează `{{authToken}}`).
12. **(Ca Alice)** **Șterge** Comentariul folosind `{{commentId}}`.
13. **(Ca Alice)** **Șterge** Postarea folosind `{{postId}}`.
14. **(Ca Admin)** **Înregistrează** un utilizator Admin, **autentifică-te**, și **testează** blocarea/deblocarea utilizatorilor.
15. **(Ca Admin)** **Șterge** un utilizator folosind ID-ul său.
16. **(Ca Admin)** **Șterge** un comentariu folosind ID-ul său.
