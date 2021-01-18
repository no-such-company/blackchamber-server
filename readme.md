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
SMail takes a new approach by communicating over the normal HTTPS protocol (port 1337) using POST/GET methods.
The advantage is that this form of communication can be quickly programmed for systems of any type.
The communication takes place via a simple specification of the sender, the recipient and an ID. The ID is determined by the sender and is used by the receiving server only to verify that the mail was actually sent by the sender.
A mail is accompanied by files in addition to the three specifications. These are ALWAYS encrypted by PGP. The sender can receive the required public key freely from the recipient's server.
A file is necessary, which determines the content of the email. A time file can contain the content additionally as HTML. All other files are considered as attachments. When the receiver and the sender check back, SHA-256 hashes are used to ensure that the files have not been tampered with in transit.

Additionally, a server can be blacklisted. As soon as a service offers SMail, it is legally obligated to state whether it uses the original procedure, and thus does not make any attempts to read or pass on the content to the user via a manipulated key or with an intermediate decryption.

If it is found that something like this has happened, the domain of a compromised service (regardless of its influence or size) will be permanently excluded. This also applies in the event of a rebranding or renaming. There is a 0 strikes rule!

In general, the server only comes into contact with the encrypted material and does not have any clear names. It works backwards only with the hash values.

## Current status

* [x] build API
* [ ] write tests
* [ ] attach Postman-Workflow for integration test
* [ ] publish sources on Github
* [ ] create first running client for Android
* [ ] setup first running service for public users

### Licence

Please read license.txt before use.

### FAQ
* **why?** GOV's always read our Mails on demand. Since the first letters was sended they tried to read them. With the new possibilities it is impossible now.
The first Time in history it is (mostly) not longer possible to take control, because everyone can use BlackChamber for his own with ease.
* **But terrorists...?** Reading Mails was always claimed as a proper way to avoid this issues. But there is no incident ever, that was prevented by reading mails.   
* **But Pedo...?** Nope... never... these a$$hole$ use other ways
* **But Druglords?** Also nope...
* **Does it supports crime?** Yes it does. In the same way as Car Manufacturer support Drugdealer by give them a way to drive around.


TL:DR;

**This Software make sure that an Email isn't longer a postcard. Nothing more, nothing less!**

### How it Works
MFE accepts JSON with File transfer via HTTPS:7331.
All Files are encrypted with PGP based on the public key of the recipient of the massage.
The Recipient is mentions with these pattern:
```url.com//:someone```

The url can be any common url. The name (someone it this example) should only `accept a-z0-9.-_`

The Message has to be introduced wit a POST Request which must include the following pattern:
```
{
    sender: "somewehere.org/someone-else"
    recipient: "url.com/someone"
    mailid: "somestring_including_timecode"
}
```
POST/Multipart to `url.com:7331/in`

Within the file there are an encrypted file with the Email text named `msg` and if needed a file called
`fmsg` which contains HTML Text if wanted.
All other Files are also encrypted.


The recipient MFE Server will now ask the sending Server if it was really send by the sender by sending the `mailid`.
GET `somewhere.org/proof/somestring_including_timecode`

The Sender should now confirm with JSON Content:
```
{
    sender: "somewehere.org/someone-else",
    recipient: "url.com/someone",
    mailid: "somestring_including_timecode",
    msg_sha512: "sha512stuffhere",
    fmsg_sha512: "only appears if html formated message was sended",
    attachments: [
    "sone_sha512_stuff_for each other file", ....
    ],
    SMail_comp: true
}
```
The recipient can check every file for the hash
`SMail_comp` is very important. With the boolean the sender will agree, that the original Dovekeeper Software is 
used und completly untapperd and no violation to break the procedure was done.
If it is TRUE and the Software is tampered or seized the owner can be sued. Also the Sender domain will be blacklisted and further 
Dovekeeper does not accept any Message send by this Domain.

The files will be stored by the Dovekeeper in the following pattern:

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
                file1(SHA-256)
                file2(SHA-256)
                meta
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

