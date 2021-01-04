# Dovekeeper

## Server for Message File Exchange with SMail

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

* [ ] build API
* [ ] write tests
* [ ] attach Postman-Workflow for integration test
* [ ] publish sources on Github
* [ ] create first running client for Android
* [ ] setup first running service for public users
* [ ] get ignored by everyone cuz who need secure communication

or 

* [ ] get first Issues
* [ ] other hosts for SMail came up

### How it Works
MFE accepts JSON with File transfer via HTTPS:7331.
All Files are encrypted with PGP based on the public key of the recipient of the massage.
The Recipient is mentions with these pattern:
```url.com//someone```

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

!! * the protective Password based on the Password, which mus hashed by SHA-256 during transport to sever. the Password will also hashed agein with same alg during the keybuild-process.

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

`@RequestParam("user") String user` username with domain
`@RequestParam("hash") String pwhash)` passphrase or user (SHA-256)/HEX

Creates a new user and setup the needed filestructure. Also the public and private key will be created.
The key can be changed later with own keys to improve security.

### delete User

`@RequestMapping(value = "/inbox/delete", method = RequestMethod.POST)`

`@RequestParam("user") String user` username with domain
`@RequestParam("hash") String pwhash)` passphrase or user (SHA-256)/HEX 

Delete all informations and files from user (can't be undone)

### fetch informations about the account

`@RequestMapping(value = "/inbox/info", method = RequestMethod.POST)`

`@RequestParam("user") String user` username with domain
`@RequestParam("hash") String pwhash)` passphrase or user (SHA-256)/HEX

Fetch some useful information from the authenticated User, like amount of files and the
overall filesize of all Files that are dedicated to the User.

    @RequestMapping(value = "/in/pubkey", method = RequestMethod.POST)
    public String fetchInboxPubKey(@RequestParam("user") String user) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/privkey", method = RequestMethod.POST)
    public String fetchInboxPubKey(@RequestParam("user") String user, @RequestParam("hash") String pwhash) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/setkeys", method = RequestMethod.POST)
    public String fetchInboxPubKey(@RequestParam("user") String user,
                                   @RequestParam("hash") String pwhash,
                                   @RequestParam("pub") MultipartFile pubKey,
                                   @RequestParam("priv") MultipartFile privKey) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/mails", method = RequestMethod.POST)
    public String fetchInboxList(@RequestParam("user") String user, @RequestParam("hash") String pwhash) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/mail", method = RequestMethod.POST)
    public String fetchMailByID(@RequestParam("user") String user, @RequestParam("hash") String pwhash, @RequestParam("mailId") String mailId) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/mail/file", method = RequestMethod.POST)
    public String fetchMailByID(@RequestParam("user") String user, @RequestParam("hash") String pwhash, @RequestParam("mailId") String mailId, @RequestParam("fileId") String fileId) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/mail/remove", method = RequestMethod.POST)
    public String removeMailByID(@RequestParam("user") String user, @RequestParam("hash") String pwhash, @RequestParam("mailId") String mailId) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/mail/move", method = RequestMethod.POST)
    public String moveMailByID(@RequestParam("user") String user, @RequestParam("hash") String pwhash, @RequestParam("mailId") String mailId) {
        return "JSON";
    }

    @RequestMapping(value = "/inbox/mail/send", method = RequestMethod.POST)
    public String sendMail(@RequestParam("attachments") List<MultipartFile> files
            , @RequestParam("user") String user
            , @RequestParam("recipients") List<String> recipients
    ) {
        return "JSON";
    }