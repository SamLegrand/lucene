from lxml import etree

context = etree.iterparse('./dataset/Posts.xml', events=('end',), tag='row')

i = 0
# lines = []
for action, elem in context:
    i += 1
    new_row = etree.Element('post', attrib={attribute: elem.attrib[attribute] for attribute in ['Id', 'ParentId', 'AcceptedAnswerId', 'CreationDate', 'LastEditDate', 'Body', 'LastEditorDisplayName', 'Title', 'Tags'] if attribute in elem.attrib})
    if 'ParentId' in elem.attrib:
        filenum = elem.attrib['ParentId']
    else:
        filenum = elem.attrib['Id']
    doc = open('./dataset/docs/%s.xml' % filenum, 'ab')
    doc.write(etree.tostring(new_row) + b'\x0a')
    doc.close()
    # lines.append(etree.tostring(new_row) + b'\x0a')
    # bytestring += etree.tostring(new_row) + b'\x0a'
    # f.write(etree.tostring(new_row) + b'\x0a')
    if i == 10000:
        break
