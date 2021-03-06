# BlackChamber

## Server for Message File Exchange with SMail

![Test](https://github.com/no-such-company/blackchamber-server/workflows/Test/badge.svg) ![Java CI with Maven](https://github.com/no-such-company/blackchamber-server/workflows/Java%20CI%20with%20Maven/badge.svg)

### Black Chamber?
The Black Chamber (1919–1929), also known as the Cipher Bureau, was the United States' first peacetime 
cryptanalytic organization, and a forerunner of the National Security Agency. The only prior 
codes and cypher organizations maintained by the US government had been some intermittent, and always 
abandoned, attempts by Armed Forces branches prior to World War I.
At least this "Service" was closed in 1929.
New Secretary of State Henry L. Stimson made this decision, and years later in his memoirs made the 
oft-quoted comment: "Gentlemen do not read each other's mail."

And this should be something, that BlackChamber attempting to bring back. 
No one should read others mails!

### Advantage of the SMail protocol
This program is the realization of a new idea of a secure replacement for email.
Messenger can not replace email, because the functionality with reply, attach, forward, etc. have a great need in communication.
The current email protocols are children of their time.
SMail takes a new approach by communicating over the normal HTTPS protocol (port 443) using simple POST/GET methods.
The advantage is that this form of communication can be quickly programmed for systems of any type.
The communication takes place via a simple specification of the sender, the recipient and an ID. The ID is determined by the sender and is used by the receiving server only to verify that the mail was actually sent by the sender.
A mail is accompanied by files in addition to the three specifications. These are ALWAYS encrypted by PGP. The sender can receive the required public key freely from the recipient's server.
A `msg` file is necessary, which determines the content of the email. A second `fmsg` file can contain the content additionally as HTML. All other files are considered as attachments. When the receiver and the sender check back, SHA-256 hashes are used to ensure that the files have not been tampered within transit.
In a later state of development the files must be signed by the sender.

Additionally, a server can be blacklisted. As soon as a service or website offers SMail, it is legally obligated to state of security whether it uses the original procedure, and thus does not make any attempts to read or pass on the content to the user via a manipulated key or with an intermediate decryption.

If it is found that something like this has happened, the domain of a compromised service (regardless of its influence or size) will be permanently excluded in a later state of development. There will be a zero strikes rule!

In general, the server only comes into contact with the encrypted material and does not have any clear names. It works backwards only with the hash values.

### Licence

Please read license.txt before use.

### FAQ
* **why?** GOV's always read our Mails on demand. Since the first letters was sended they tried to read them. With the new possibilities it is impossible now.
The first Time in history it is (mostly) not longer possible to take control, because everyone can use BlackChamber for his own with ease.
* **But terrorists...?** Reading Mails was always claimed as a proper way to avoid this issues. But there is no incident ever, that was prevented by reading mails.
* **But criminals...?** Also nope...
* **Does it supports crime?** Yes it does. In the same way as Car Manufacturer support Drugdealer by give them a way to drive around.


TL:DR;

**This Software make sure that an Email isn't longer a postcard. Nothing more, nothing less!**

### Port
The default port is 443. It can be changed. SMail just try to send and awaits a clean API without expectations. You can run BlackChamber on a subdomain.
The Address is now related to the subdomain.
Example: `mail.url.com//:user.name`

[Read here](https://github.com/no-such-company/blackchamber-server/wiki/Setup-Host-forward) if you want to use your TLD and run Blackchamber on Subdomain


### How it Works
BlackChamber accepts JSON with File transfer via HTTPS:443 (or different port in experimental cases).
All Files are encrypted with PGP based on the public key of the recipient of the massage.
The Recipient is mentions with these pattern:
```url.com//:someone```

The url can be any common url. The name (someone it this example) should only `accept a-z0-9.-_`
Within the POST to send Emails, there are an encrypted file with the Email text named `msg` and if needed a file called
`fmsg` which contains HTML Text if wanted.
All other Files are also encrypted. See #profil section for further information.


### Content Probing
The recipient BlackChamber Server will now ask the sending Server if it was really send by the sender by sending the `mailid`.
GET `somewhere.org/probe/mailId`
The security concept provides that after receiving a mail, the sending server is confronted with the hash value of the sent files. However, only half of the hash is transmitted in each case.
The sending server must confirm with the full hash value that it is the legitimate sender.

The Sender should now confirm with JSON Content:
```
{
    sender: "somewehere.org//:someone-else",
    recipient: "url.com//:someone",
    mailid: "somestring_including_timecode",
    attachments: [
    "sone_sha512_stuff_for each other file", ....
    ]
}

```
Furthermore, the client software protocol requires that the files are signed by the sender.
Later clients are only allowed as official clients if a signing takes place in addition to the PGP encryption.

The files will be stored by the BlackChamber in the following pattern:

```
dovecote/
    username(SHA-256)/
        pub.asc (PGP public)
        key.skr (PGP private pw-protected) *
        hashed-username(SHA-256).pw **
        in/
            mail-ID(SHA-256)/
                msg
                fmsg
                meta
                attachment/
                    file1
                    file2
            mail-ID(SHA-256)/
                msg
                fmsg
                meta
        random_folder_name/
            mail-ID(SHA-256)/
                msg
                fmsg
                meta
        out/
            mail-ID(SHA-256)/
                msg
                fmsg
                meta                
```

!! * the protective Password based on the Password, which must hashed by SHA-256 during transport to sever. the Password will also hashed again with same alg during the keybuild-process.

!! ** the Password that is used, is the ( username + SHA-256 protected Password ) generated SHA-256.
  

## API

### send Mail to Recipient
`@RequestMapping(value = "/in", method = RequestMethod.POST)`

* `@RequestParam("sender") String sender` sender of incoming Mail
* `@RequestParam("recipient") String recipient` the recipient of the incoming Mail
* `@RequestParam("mailId") String mailId` the mailId given by sending Server
* `@RequestParam("attachments") List<MultipartFile> files` the files

There must be an `msg` file attached otherwise the server will not accept it.
An encrypted HTML-Text File is optional (must be named `fmsg`)
All other files are optional and threaten as attachment.

### Validation endpoint for Mails

`@RequestMapping(value = "/in/probe", method = RequestMethod.POST)`

* `public ResponseEntity probeSendMail(@RequestBody Probe probeModel)` the JSON to Probe if the Mail was send by the sender.
To ensure that the content was untouched, the client of the recipient can probe it.
The Strings in `attachment` are the SHA-512 of the content of each file, without snitching what file is meant.

The JSON example:
```
{
    mailId : "String",
    sender : "String",
    recipient : "String",
    attachments : ["String"]
}
```

### create new user

`@RequestMapping(value = "/inbox/create", method = RequestMethod.POST)`

* `@RequestParam("user") String user` username with domain
* `@RequestParam("hash") String pwhash)` passphrase for Mailboxfuctions (SHA-256)/HEX
* `@RequestParam("keyHash") String keyHash)` passphrase for PGP Key (SHA-256)/HEX

PGP Key phrase and Mailbox password cannot be equals.
Creates a new user and setup the needed filestructure. Also the public and private key will be created.
The key can be changed later with own keys to improve security.

### delete User

`@RequestMapping(value = "/inbox/delete", method = RequestMethod.POST)`

* `@RequestParam("user") String user` username with domain
* `@RequestParam("hash") String pwhash)` passphrase or user (SHA-256)/HEX 

Delete all informations and files from user (can't be undone)

### fetch information about the account

`@RequestMapping(value = "/inbox/info", method = RequestMethod.POST)`

* `@RequestParam("user") String user` username with domain
* `@RequestParam("hash") String pwhash)` passphrase or user (SHA-256)/HEX

Fetch some useful information from the authenticated User, like amount of files and the
overall filesize of all Files that are dedicated to the User.

### fetch public key from a possible recipient

`@RequestMapping(value = "/in/pubkey", method = RequestMethod.POST)`

* `@RequestParam("user")` username of recipient with domain

### fetch own private key

`@RequestMapping(value = "/inbox/privkey", method = RequestMethod.POST)`

* `@RequestParam("user") String user` username with domain
* `@RequestParam("hash") String pwhash)` passphrase or user (SHA-256)/HEX

### add own keypair from other source

`@RequestMapping(value = "/inbox/setkeys", method = RequestMethod.POST)`

* `@RequestParam("user") String user` username with domain
* `@RequestParam("hash") String pwhash` passphrase or user (SHA-256)/HEX
* `@RequestParam("pub") MultipartFile pubKey` a public key file (must named: )
* `@RequestParam("priv") MultipartFile privKey)` a private key file (must named: )

This option add some more security. So you can create own keys without trusting the mailserver owner.
Keep in mind, that you have to add a passphrase to the private key, because future mail clients will not work
without.

### renew keys on server

`@RequestMapping(value = "/inbox/renewkeys", method = RequestMethod.POST)`

* `@RequestParam("user") String user` username with domain
* `@RequestParam("hash") String pwhash)` passphrase for Mailboxfuctions (SHA-256)/HEX
* `@RequestParam("keyHash") String keyHash)` passphrase for PGP Key (SHA-256)/HEX

Could be used to create a new keypair and overwrite the existsing.

**!!! Keep in mind, that all mails before could not longer be decrypted !!!**

### fetch map of all mailID's with folders

`@RequestMapping(value = "/inbox/mails", method = RequestMethod.POST)`

* `@RequestParam("user") String user` username with domain
* `@RequestParam("hash") String pwhash)` passphrase for Mailboxfuctions (SHA-256)/HEX

Retireve a JSON with all Folders and the ID hashes of the mails in it.
Example:
```

{
    "folder": [
        {
            "folderName": "out",
            "mails": []
        },
        {
            "folderName": "in",
            "mails": [
                {
                    "mailId": "b8a8763d63bfa91ec7286f5084d7d679b7ccac39214063a8dd41a032ae59100b",
                    "attachments": [
                        "attachment1",
                        "attachment2"
                    ],
                    "mailDescriptors": [
                        "msg",
                        "fmsg",
                        "meta"
                    ]
                }
            ]
        }
    ]
}

```

### fetch mail content
`@RequestMapping(value = "/inbox/mail", method = RequestMethod.POST)`

* `@RequestParam("mailId") String mailId` ID of the mail, that should be fetched
* `@RequestParam("user") String user` username with domain
* `@RequestParam("hash") String pwhash)` passphrase for Mailboxfuctions (SHA-256)/HEX

Return all filenames inside the mailID

### fetch file from mailID

`@RequestMapping(value = "/inbox/mail/file", method = RequestMethod.POST)`

* `@RequestParam("user") String user` username with domain
* `@RequestParam("hash") String pwhash` passphrase for Mailboxfuctions (SHA-256)/HEX
* `@RequestParam("mailId") String mailId` ID of the Mail
* `@RequestParam("folder") String folder` Name of the Folder
* `@RequestParam("fileId") String fileId` ID of the File
  
Fetch a specific file regarding to the mail ID

### remove Mail from server

`@RequestMapping(value = "/inbox/mail/remove", method = RequestMethod.POST)`

* `@RequestParam("user") String user` username with domain
* `@RequestParam("hash") String pwhash` passphrase for Mailboxfuctions (SHA-256)/HEX
* `@RequestParam("mailId") String mailId` ID of the Mail

### move mail to new folder

`@RequestMapping(value = "/inbox/mail/move", method = RequestMethod.POST)`

* `@RequestParam("user") String user` username with domain
* `@RequestParam("hash") String pwhash` passphrase for Mailboxfuctions (SHA-256)/HEX
* `@RequestParam("mailId") String mailId` ID of the Mail
* `@RequestParam("dest") String dest` String of the new destination for the Mail

### send a mail

`@RequestMapping(value = "/inbox/mail/send", method = RequestMethod.POST)`

* `@RequestParam("attachments") List<MultipartFile> files` files with the mail content
* `@RequestParam("user") String user` username with domain
* `@RequestParam("recipients") List<String> recipients` list of recipient (actually we can just use one, becuase of the encryption.)

## Message profil

To send a proper message you need to create a `msg` file that is encrypted by the recipients and senders public key.
Also all other files should be done with the same encryption. If you do not add your own public key, you can't 
read your send mails from the outbox at least.
These files will be encrypted with the public key of the sender.

The JSON example of a `msg` file:
```
{
    subject : "Here comes the subject of the Smail",
    content : "Here comes plaintext of the content",
    sender : "some.com//:foo.bar"
}
```

There can be HTML content too, that is named `fmsg`. This is just a representation of the content.
Subject and Sender will be fetched from the original `msg` file.


## TLS

To use SSL/TLS you have to add a proper .properties configuration on startup:

`java -jar app.jar --spring.config.location=ssl.properties`

Keep in mind, that the default configuration will not used anymore.
So you have to add along the new SSL Configuration part, the original values to run the server.
Please do not change the port. Otherwise, other SMail Server can't communicate with your instance.

Here is an Example for this file:

```java
# SSL
server.ssl.key-store=/home/user/keys/cert.p12
server.ssl.key-store-password=123456

# JKS or PKCS12
server.ssl.keyStoreType=PKCS12

# Spring Security
# uncomment if the server only accept secured layer
# security.require-ssl=true

# These values can be modified
spring.servlet.multipart.max-file-size=128KB
spring.servlet.multipart.max-request-size=128KB

#Server port
server.port=443

# These part must remain untouched
spring.servlet.multipart.enabled=true
spring.servlet.multipart.location=${java.io.tmpdir}
server.error.whitelabel.enabled=false
bc.version=@project.version@

```

## Setup on Hosts
You can run BlackChamber under a subdomain or with IP.

Example:
You run BlackChamber on subdomain `mail.your-domain.com`.
This might result in a longer SMail Address.
At last your SMail Address would look like: `mail.your-domain.com//:user.name`

Solution:
Add a file named `smail.redir` into the accessible root of your TLD and point to the Server that holds your BlackChamber Server.
This file contains the location of the Server where BlackChamber is reached.
```
mail.your-domain.com
```

After doing so, the SMail Address will be your-domain.com//:user.name
The port can be changed withing the url (like `mail.your-domain.com:8080`) if you want. Everything else than Port 80 and 443 will be very experimental.
Please note, that your Users have to work with the domain they are registered for. Mixing of both didn't work.

